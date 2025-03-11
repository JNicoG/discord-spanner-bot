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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static io.github.jnicog.discord.spanner.bot.service.QueueInteractionOutcome.ADDED_TO_QUEUE;
import static io.github.jnicog.discord.spanner.bot.service.QueueInteractionOutcome.ALREADY_IN_QUEUE;
import static io.github.jnicog.discord.spanner.bot.service.QueueInteractionOutcome.ALREADY_NOT_IN_QUEUE;
import static io.github.jnicog.discord.spanner.bot.service.QueueInteractionOutcome.QUEUE_ALREADY_FULL;
import static io.github.jnicog.discord.spanner.bot.service.QueueInteractionOutcome.REMOVED_FROM_QUEUE;

@Service
public class QueueServiceImpl implements QueueService {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueServiceImpl.class);

    private final Map<User, Long> playerQueue = new LinkedHashMap<User, Long>(MAX_QUEUE_SIZE);
    private static boolean isQueuePoppedState = false;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<User, ScheduledFuture<?>> timeoutTasks = new ConcurrentHashMap<>();

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

        if (playerQueue.containsKey(user)) {
            notifyService.sendReply(slashCommandInteractionEvent, ALREADY_IN_QUEUE.getDescription(), true);
            return;
        }

        if (playerQueue.size() >= MAX_QUEUE_SIZE) {
            notifyService.sendReply(slashCommandInteractionEvent, QUEUE_ALREADY_FULL.getDescription(), true);
            return;
        }

        playerQueue.put(user, System.currentTimeMillis());
        ScheduledFuture<?> timeoutKeen =
                scheduler.schedule(() -> silentRemoveFromPlayerQueue(user), USER_TIMEOUT, TimeUnit.HOURS);
        timeoutTasks.put(user, timeoutKeen);
        notifyService.sendReply(slashCommandInteractionEvent,
                String.format("%s%s [%d/5]", user.getAsMention(), ADDED_TO_QUEUE.getDescription(), playerQueue.size()),
                false);

        if (isPlayerQueueFull() && !getQueuePoppedState()) {
            setQueuePoppedState();
            notifyService.notifyPlayerQueuePopped(
                    playerQueue.keySet(), slashCommandInteractionEvent.getMessageChannel());
        }
    }

    @Override
    public synchronized void leavePlayerQueue(SlashCommandInteractionEvent slashCommandInteractionEvent) {
        User user = slashCommandInteractionEvent.getUser();

        if (!playerQueue.containsKey(user)) {
            notifyService.sendReply(
                    slashCommandInteractionEvent, ALREADY_NOT_IN_QUEUE.getDescription(), true);
            return;
        }

        playerQueue.remove(slashCommandInteractionEvent.getUser());
        ScheduledFuture<?> timeoutTask = timeoutTasks.remove(user);
        if (timeoutTask != null) {
            timeoutTask.cancel(false);
        }
        notifyService.sendReply(slashCommandInteractionEvent,
                String.format("%s%s [%d/5]",
                        user.getAsMention(),
                        REMOVED_FROM_QUEUE.getDescription(),
                        playerQueue.size()),
                false);

        if (getQueuePoppedState()) {
            unsetQueuePoppedState();
        }
    }

    @Override
    public synchronized List<User> removeFromPlayerQueue(List<User> removeFromPlayerQueueList) {
        List<User> playersRemovedFromQueue = new ArrayList<>(5);

        for (User user : removeFromPlayerQueueList) {
            if (!playerQueue.containsKey(user)) {
                throw new IllegalArgumentException(
                        String.format("Invalid user list provided. " +
                                        "Failed to remove user %s from player queue " +
                                        "(specified user is not in the player queue)",
                                user.getIdLong()));
            }
            playerQueue.remove(user);
            playersRemovedFromQueue.add(user);
        }
        return playersRemovedFromQueue;
    }

    @Override
    public synchronized void silentRemoveFromPlayerQueue(User user) {
        if (!playerQueue.containsKey(user)) {
            throw new IllegalArgumentException(
                    String.format("Invalid user provided. " +
                                    "Failed to remove user %s from player queue " +
                                    "(specified user is not in the player queue)",
                            user.getIdLong()));
        }
        playerQueue.remove(user);
        LOGGER.info(String.format("User %s has been timed out from the queue.", user.getName()));
    }

    @Override
    public synchronized Set<User> getPlayerQueue() {
        return playerQueue.keySet();
    }

    public boolean isPlayerQueueFull() {
        return getPlayerQueue().size() == MAX_QUEUE_SIZE;
    }

    public boolean getQueuePoppedState() {
        return isQueuePoppedState;
    }

    public void setQueuePoppedState() {
        isQueuePoppedState = true;
    }

    public void unsetQueuePoppedState() {
        isQueuePoppedState = false;
    }

    @Override
    public synchronized void resetPlayerQueue() {
        playerQueue.clear();
        unsetQueuePoppedState();
    }

}
