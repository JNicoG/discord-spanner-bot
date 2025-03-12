package io.github.jnicog.discord.spanner.bot.service;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.github.jnicog.discord.spanner.bot.service.QueueInteractionOutcome.ADDED_TO_QUEUE;
import static io.github.jnicog.discord.spanner.bot.service.QueueInteractionOutcome.ALREADY_IN_QUEUE;
import static io.github.jnicog.discord.spanner.bot.service.QueueInteractionOutcome.ALREADY_NOT_IN_QUEUE;
import static io.github.jnicog.discord.spanner.bot.service.QueueInteractionOutcome.QUEUE_ALREADY_FULL;
import static io.github.jnicog.discord.spanner.bot.service.QueueInteractionOutcome.REMOVED_FROM_QUEUE;

@Service
public class QueueServiceImpl implements QueueService {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueServiceImpl.class);

    private final Map<User, Long> PLAYER_QUEUE = new LinkedHashMap<>(MAX_QUEUE_SIZE);
    private boolean isQueuePoppedState = false;

    private final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);
    private final Map<User, ScheduledFuture<?>> TIMEOUT_TASKS_MAP = new ConcurrentHashMap<>();

    private static final int MAX_QUEUE_SIZE = 5;

    private static final int USER_TIMEOUT = 1;

    private static final int POPPED_QUEUE_TIMEOUT = 180;

    @Autowired
    private final NotifyService notifyService;

    public QueueServiceImpl(NotifyService notifyService) {
        this.notifyService = notifyService;
    }

    @Override
    public synchronized void joinPlayerQueue(SlashCommandInteractionEvent slashCommandInteractionEvent) {
        User user = slashCommandInteractionEvent.getUser();

        if (getPlayerQueue().containsKey(user)) {
            notifyService.sendReply(slashCommandInteractionEvent, ALREADY_IN_QUEUE.getDescription(), true);
            return;
        }

        if (getPlayerQueue().size() >= MAX_QUEUE_SIZE) {
            notifyService.sendReply(slashCommandInteractionEvent, QUEUE_ALREADY_FULL.getDescription(), true);
            return;
        }

        addUserToPlayerQueue(user);
        notifyService.sendReply(slashCommandInteractionEvent,
                String.format("%s%s [%d/%d]",
                        user.getAsMention(),
                        ADDED_TO_QUEUE.getDescription(),
                        getPlayerQueue().size(),
                        MAX_QUEUE_SIZE),
                false);

        if (isPlayerQueueFull() && !getQueuePoppedState()) {
            setQueuePoppedState();
            notifyService.notifyPlayerQueuePopped(
                    getPlayerQueue().keySet(), slashCommandInteractionEvent.getMessageChannel());
        }
    }

    @Override
    public synchronized void leavePlayerQueue(SlashCommandInteractionEvent slashCommandInteractionEvent) {
        User user = slashCommandInteractionEvent.getUser();

        if (!getPlayerQueue().containsKey(user)) {
            notifyService.sendReply(
                    slashCommandInteractionEvent, ALREADY_NOT_IN_QUEUE.getDescription(), true);
            return;
        }

        removeUserFromPlayerQueue(slashCommandInteractionEvent.getUser());
        notifyService.sendReply(slashCommandInteractionEvent,
                String.format("%s%s [%d/%d]",
                        user.getAsMention(),
                        REMOVED_FROM_QUEUE.getDescription(),
                        getPlayerQueue().size(),
                        MAX_QUEUE_SIZE),
                false);

        if (getQueuePoppedState()) {
            unsetQueuePoppedState();
        }
    }

    private synchronized void addUserToPlayerQueue(User user) {
        getPlayerQueue().put(user, System.currentTimeMillis());
        ScheduledFuture<?> timeoutKeen =
                SCHEDULER.schedule(() -> removeKeenByTimeout(user), USER_TIMEOUT, TimeUnit.HOURS);
        TIMEOUT_TASKS_MAP.put(user, timeoutKeen);
    }

    @Override
    public synchronized List<User> removeUserFromPlayerQueue(List<User> userList) {
        List<User> playersRemovedFromQueue = new ArrayList<>(5);

        for (User user : userList) {
            removeUserFromPlayerQueue(user);
            playersRemovedFromQueue.add(user);
        }
        LOGGER.info(String.format("Users %s have been removed from the queue.",
                playersRemovedFromQueue.stream().map(User::getName).toList()));
        return playersRemovedFromQueue;
    }

    private synchronized void removeKeenByTimeout(User user) {
        LOGGER.info(String.format("User %s has reached the timeout limit due to inactivity.", user.getName()));
        /**
         * TODO:
         * If queue popped, do not remove by timeout - delay ScheduledFuture by 5 minutes?
         */
        removeUserFromPlayerQueue(user);
    }

    @Override
    public synchronized void removeUserFromPlayerQueue(User user) {
        if (!getPlayerQueue().containsKey(user)) {
            throw new IllegalArgumentException(
                    String.format("Invalid user provided. " +
                                    "Failed to remove user %s from player queue " +
                                    "(specified user is not in the player queue)",
                            user.getIdLong()));
        }
        getPlayerQueue().remove(user);
        ScheduledFuture<?> timeoutTask = TIMEOUT_TASKS_MAP.remove(user);
        if (timeoutTask != null) {
            timeoutTask.cancel(false);
        }
        LOGGER.info(String.format("User %s has been removed from the queue.", user.getName()));
    }

    @Override
    public void showQueue(SlashCommandInteractionEvent slashCommandInteractionEvent) {
        notifyService.sendSilentReply(slashCommandInteractionEvent,
                String.format("[%d/%d] Current keeners: %s",
                        getPlayerQueue().size(),
                        MAX_QUEUE_SIZE,
                        getPlayerQueue()
                                .keySet()
                                .stream()
                                .map(User::getAsMention)
                                .collect(Collectors.joining(", "))));
    }

    public synchronized Map<User, Long> getPlayerQueue() {
        return this.PLAYER_QUEUE;
    }

    public boolean isPlayerQueueFull() {
        return getPlayerQueue().size() >= MAX_QUEUE_SIZE;
    }

    public boolean getQueuePoppedState() {
        return this.isQueuePoppedState;
    }

    public void setQueuePoppedState() {
        this.isQueuePoppedState = true;
        /**
         * TODO:
         * Add timeout tasks for each user to react / "accept" queue within POPPED_QUEUE_TIMEOUT
         */
    }

    public void unsetQueuePoppedState() {
        this.isQueuePoppedState = false;
    }

    public Map<User, ScheduledFuture<?>> getTimeoutTasksMap() {
        return this.TIMEOUT_TASKS_MAP;
    }

    @Override
    public synchronized void resetPlayerQueue() {
        /**
         * Handle scheduled timeout tasks
         */
        getPlayerQueue().clear();
        unsetQueuePoppedState();
    }

}
