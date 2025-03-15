package io.github.jnicog.discord.spanner.bot.model;

import io.github.jnicog.discord.spanner.bot.config.QueueProperties;
import io.github.jnicog.discord.spanner.bot.event.CheckInCancelledEvent;
import io.github.jnicog.discord.spanner.bot.event.CheckInCompletedEvent;
import io.github.jnicog.discord.spanner.bot.event.CheckInStartedEvent;
import io.github.jnicog.discord.spanner.bot.event.CheckInTimeoutEvent;
import io.github.jnicog.discord.spanner.bot.event.PlayerTimeoutEvent;
import io.github.jnicog.discord.spanner.bot.service.QueueEventPublisher;
import io.github.jnicog.discord.spanner.bot.service.SpannerService;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ChannelQueue {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelQueue.class);

    private final MessageChannel messageChannel;
    private final QueueProperties queueProperties;
    private final SpannerService spannerService;
    private final QueueEventPublisher eventPublisher;

    // Core queue
    private final Map<User, Long> playerQueue = new ConcurrentHashMap<>();
    private final Map<User, ScheduledFuture<?>> timeoutTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // Check-in state
    private volatile boolean checkInActive = false;
    private final Map<User, Boolean> checkInStatusMap = new ConcurrentHashMap<>();
    private volatile ScheduledFuture<?> checkInTimeoutTask;
    private volatile long lastActiveCheckInMessageId = -1;

    private volatile Instant lastActivityTime = Instant.now();

    public ChannelQueue(MessageChannel messageChannel,
                        QueueProperties queueProperties,
                        SpannerService spannerService,
                        QueueEventPublisher eventPublisher) {
        this.messageChannel = messageChannel;
        this.queueProperties = queueProperties;
        this.spannerService = spannerService;
        this.eventPublisher = eventPublisher;
    }

    public synchronized boolean addPlayer(User user, MessageChannel messageChannel) {
        lastActivityTime = Instant.now();
        long messageChannelId = messageChannel.getIdLong();

        if (playerQueue.containsKey(user)) {
            LOGGER.info("User {} is already in the queue for channel {}", user.getName(), messageChannelId);
            return false;
        }

        if (playerQueue.size() >= queueProperties.getMaxQueueSize()) {
            LOGGER.info("Queue is already full for channel {}", messageChannelId);
            return false;
        }

        playerQueue.put(user, System.currentTimeMillis());
        schedulePlayerTimeout(user);

        LOGGER.info("Added user {} to queue for channel {}, queue size: {}/{}",
                user.getName(), messageChannelId, playerQueue.size(), queueProperties.getMaxQueueSize());

        if (playerQueue.size() >= queueProperties.getMaxQueueSize()) {
            LOGGER.info("Queue filled in channel {}, initiating check-in", messageChannelId);
            initiateCheckIn(messageChannel);
        }

        return true;
    }

    private void schedulePlayerTimeout(User user) {
        ScheduledFuture<?> playerTimeoutTask = scheduler.schedule(
                () -> handlePlayerTimeout(user),
                queueProperties.getUserTimeoutLength(),
                queueProperties.getUserTimeoutUnit()
        );
        timeoutTasks.put(user, playerTimeoutTask);

        LOGGER.debug("Scheduled timeout for user {} in {} {}",
                user.getName(), queueProperties.getUserTimeoutLength(), queueProperties.getUserTimeoutUnit());

    }

    private void handlePlayerTimeout(User user) {
        LOGGER.info("User {} has timed out in channel {}", user.getName(), getChannelId());

        if (removePlayer(user, false)) {
            eventPublisher.publishPlayerTimeoutEvent(new PlayerTimeoutEvent(this, user));
        }
    }

    private synchronized void initiateCheckIn(MessageChannel messageChannel) {
        checkInActive = true;

        playerQueue.keySet().forEach(user -> checkInStatusMap.put(user, false));
        checkInTimeoutTask = scheduler.schedule(
                () -> handleCheckInTimeout(messageChannel),
                queueProperties.getCheckInTimeoutLength(),
                queueProperties.getCheckInTimeoutUnit()
        );

        /**
         * TODO: Handle existing player timeout tasks - give each player fresh timeout tasks
         * Ensure timeout tasks are always longer than the check-in timeout length
         */

        LOGGER.info("Check-in started for channel {}, waiting {} {} for {} players to accept...",
                messageChannel, queueProperties.getCheckInTimeoutLength(),
                queueProperties.getCheckInTimeoutUnit().toString().toLowerCase(),
                queueProperties.getMaxQueueSize());

        eventPublisher.publishCheckInStartedEvent(new CheckInStartedEvent(this, messageChannel));

    }

    public synchronized boolean playerCheckIn(User user, MessageChannel channel) {
        lastActivityTime = Instant.now();

        if (!checkInActive || !checkInStatusMap.containsKey(user)) {
            LOGGER.info("Invalid check-in from user {} in channel {} - no active check-in or not in queue",
                user.getName(), channel.getIdLong());
            return false;
        }

        boolean allCheckedIn = checkInStatusMap.values().stream().allMatch(Boolean::booleanValue);
        if (allCheckedIn) {
            LOGGER.info("All players checked in for channel {}, completing check-in", channel.getIdLong());
            completeCheckIn(channel);
        }

        return true;
    }

    private synchronized void handleCheckInTimeout(MessageChannel messageChannel) {
        if (!checkInActive) {
            return;
        }

        Set<User> notCheckedIn = checkInStatusMap.entrySet().stream()
                .filter(entry -> entry.getValue().equals(false))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        LOGGER.info("Players {} in channel {} did not check-in on time during the check-in period",
                notCheckedIn, messageChannel.getIdLong());

        notCheckedIn.forEach(user -> {
            removePlayer(user, true);
        });

        eventPublisher.publishCheckInTimeoutEvent(new CheckInTimeoutEvent(this, messageChannel, notCheckedIn));

        resetCheckIn();
    }

    private synchronized void completeCheckIn(MessageChannel channel) {
        resetCheckIn();

        LOGGER.info("Check-in completed for channel {}", messageChannel.getIdLong());
        eventPublisher.publishCheckInCompletedEvent(new CheckInCompletedEvent(this, channel));
    }

    public synchronized void cancelCheckIn(MessageChannel channel, User user) {
        resetCheckIn();

        LOGGER.info("Check-in cancelled for channel {} by user {}", messageChannel.getIdLong(), user.getName());
        eventPublisher.publishCheckInCancelledEvent(new CheckInCancelledEvent(this, channel, user));
    }

    private synchronized void resetCheckIn() {
        if (checkInTimeoutTask != null) {
            checkInTimeoutTask.cancel(false);
        }

        checkInActive = false;
        checkInStatusMap.clear();
    }

    public synchronized boolean removePlayer(User user, boolean applySpanner) {
        lastActivityTime = Instant.now();

        if (!playerQueue.containsKey(user)) {
            return false;
        }

        playerQueue.remove(user);

        ScheduledFuture<?> timeoutTask = timeoutTasks.remove(user);
        if (timeoutTask != null) {
            timeoutTask.cancel(false);
        }

        if (applySpanner) {
            LOGGER.info("Applying spanner to user {} in channel {}", user.getName(), messageChannel.getIdLong());
            spannerService.incrementSpannerCount(user.getIdLong());
        }

        if (checkInActive) {
            cancelCheckIn(messageChannel, user);
        }

        return true;
    }

    public long getChannelId() {
        return messageChannel.getIdLong();
    }

    public boolean isEmpty() {
        return playerQueue.isEmpty();
    }

    public boolean isFull() {
        return playerQueue.size() >= queueProperties.getMaxQueueSize();
    }

    public Set<User> getPlayers() {
        return playerQueue.keySet();
    }

    public boolean isCheckInActive() {
        return checkInActive;
    }

    public Map<User, Boolean> getCheckInStatus() {
        return new ConcurrentHashMap<>(checkInStatusMap);
    }

    public Instant getLastActivityTime() {
        return lastActivityTime;
    }

    public void shutdown() {
        LOGGER.info("Shutting down queue for channel {}", getChannelId());

        timeoutTasks.values().forEach(task -> task.cancel(false));
        timeoutTasks.clear();

        if (checkInTimeoutTask != null) {
            checkInTimeoutTask.cancel(false);
        }

        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS))  {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            scheduler.shutdownNow();
        }
    }

}
