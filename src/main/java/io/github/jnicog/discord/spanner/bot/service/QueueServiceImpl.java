package io.github.jnicog.discord.spanner.bot.service;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    private final Map<User, Long> PLAYER_QUEUE = new ConcurrentHashMap<>(MAX_QUEUE_SIZE);

    private final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);
    private final Map<User, ScheduledFuture<?>> TIMEOUT_TASKS_MAP = new ConcurrentHashMap<>();

    // TODO: Move queue properties
    public static final int MAX_QUEUE_SIZE = 5;
    private static final int USER_TIMEOUT_LENGTH = 1;
    private static final TimeUnit USER_TIMEOUT_UNIT = TimeUnit.HOURS;

    public QueueServiceImpl() {
        // Do not instantiate
    }

    @Override
    public QueueInteractionOutcome joinPlayerQueue(SlashCommandInteractionEvent slashCommandInteractionEvent) {
        if (getPlayerQueue().containsKey(slashCommandInteractionEvent.getUser())) {
            return ALREADY_IN_QUEUE;
        }

        if (getPlayerQueue().size() >= MAX_QUEUE_SIZE) {
            return QUEUE_ALREADY_FULL;
        }

        addUserToPlayerQueue(slashCommandInteractionEvent);

        return ADDED_TO_QUEUE;
    }

    @Override
    public QueueInteractionOutcome leavePlayerQueue(SlashCommandInteractionEvent slashCommandInteractionEvent) {
        User user = slashCommandInteractionEvent.getUser();

        if (!getPlayerQueue().containsKey(user)) {
            return ALREADY_NOT_IN_QUEUE;
        }

        removeUserFromPlayerQueue(slashCommandInteractionEvent.getUser());
        return REMOVED_FROM_QUEUE;
    }

    private void addUserToPlayerQueue(SlashCommandInteractionEvent slashCommandInteractionEvent) {
        User user = slashCommandInteractionEvent.getUser();

        getPlayerQueue().put(user, System.currentTimeMillis());

        ScheduledFuture<?> timeoutKeen =
                SCHEDULER.schedule(() -> removeKeenByTimeout(user), USER_TIMEOUT_LENGTH, USER_TIMEOUT_UNIT);
        TIMEOUT_TASKS_MAP.put(user, timeoutKeen);

    }

    @Override
    public List<User> removeUserFromPlayerQueue(List<User> userList) {
        List<User> playersRemovedFromQueue = new ArrayList<>(5);

        for (User user : userList) {
            removeUserFromPlayerQueue(user);
            playersRemovedFromQueue.add(user);
        }

        LOGGER.info(String.format("Users %s have been removed from the queue.",
                playersRemovedFromQueue.stream().map(User::getName).toList()));

        return playersRemovedFromQueue;
    }

    private void removeKeenByTimeout(User user) {
        LOGGER.info(String.format("User %s has reached the timeout limit due to inactivity.", user.getName()));
        // TODO: If queue popped, do not remove by timeout - delay ScheduledFuture by 5 minutes?
        removeUserFromPlayerQueue(user);
    }

    @Override
    public void removeUserFromPlayerQueue(User user) {
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
    public Set<User> showQueue() {
        return getPlayerQueue().keySet();
    }

    public Map<User, Long> getPlayerQueue() {
        return this.PLAYER_QUEUE;
    }

    public boolean isPlayerQueueFull() {
        return getPlayerQueue().size() >= MAX_QUEUE_SIZE;
    }

    protected Map<User, ScheduledFuture<?>> getTimeoutTasksMap() {
        return this.TIMEOUT_TASKS_MAP;
    }

    @Override
    public synchronized void resetPlayerQueue() {
        getTimeoutTasksMap().values().forEach(scheduledFuture -> scheduledFuture.cancel(false));
        getTimeoutTasksMap().clear();
        getPlayerQueue().clear();
    }

}
