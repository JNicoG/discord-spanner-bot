package io.github.jnicog.discord.spanner.bot.model;

import io.github.jnicog.discord.spanner.bot.config.QueueProperties;
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
import java.util.stream.Collectors;

public class ChannelQueue {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelQueue.class);

    private final MessageChannel messageChannel;
    private final QueueProperties queueProperties;
    private final SpannerService spannerService;

    // Core queue
    private final Map<User, Long> playerQueue = new ConcurrentHashMap<>();
    private final Map<User, ScheduledFuture<?>> timeoutTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // Check-in state
    private volatile boolean checkInActive = false;
    private final Map<User, Boolean> checkInStatusMap = new ConcurrentHashMap<>();
    private volatile ScheduledFuture<?> checkInTimeoutTask;
    private volatile long activeCheckInMessageId = -1;

    private volatile Instant lastActivityTime = Instant.now();

    public ChannelQueue(MessageChannel messageChannel, QueueProperties queueProperties, SpannerService spannerService) {
        this.messageChannel = messageChannel;
        this.queueProperties = queueProperties;
        this.spannerService = spannerService;
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

    }

    private synchronized void initiateCheckIn(MessageChannel messageChannel) {
        checkInActive = true;

        playerQueue.keySet().forEach(user -> checkInStatusMap.put(user, false));
        checkInTimeoutTask = scheduler.schedule(
                () -> handleCheckInTimeout(messageChannel),
                queueProperties.getCheckInTimeoutLength(),
                queueProperties.getCheckInTimeoutUnit()
        );

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

        cancelCheckIn();
    }

    public synchronized void cancelCheckIn() {
        if (checkInTimeoutTask != null) {
            checkInTimeoutTask.cancel(false);
        }

        checkInActive = false;
        checkInStatusMap.clear();
        activeCheckInMessageId = -1;

        LOGGER.info("Check-in cancelled for channel {}", messageChannel.getIdLong());
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
            cancelCheckIn();
        }

        return true;
    }


    public long getChannelId() {
        return messageChannel.getIdLong();
    }
}
