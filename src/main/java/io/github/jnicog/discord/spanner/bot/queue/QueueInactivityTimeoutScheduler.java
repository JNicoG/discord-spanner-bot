package io.github.jnicog.discord.spanner.bot.queue;

import io.github.jnicog.discord.spanner.bot.checkin.CheckInSessionReader;
import io.github.jnicog.discord.spanner.bot.config.QueueProperties;
import io.github.jnicog.discord.spanner.bot.event.queue.PlayerQueueTimeoutEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Manages scheduling and cancellation of queue inactivity timeout tasks.
 *
 * <p>When a user joins the queue, a timeout is scheduled. If the queue doesn't
 * fill within the timeout period, the user is removed from the queue.</p>
 *
 * <p>Key behaviours:</p>
 * <ul>
 *   <li>Timeouts are cancelled when a check-in session starts (queue is full)</li>
 *   <li>Timeouts are cancelled when a user manually leaves the queue</li>
 *   <li>If a timeout fires during an active check-in, it is deferred until the session ends</li>
 *   <li>Users restored to queue after check-in cancellation/timeout do NOT get new timeouts</li>
 * </ul>
 */
@Component
public class QueueInactivityTimeoutScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueInactivityTimeoutScheduler.class);

    private final TaskScheduler taskScheduler;
    private final QueueService queueService;
    private final CheckInSessionReader checkInSessionReader;
    private final ApplicationEventPublisher eventPublisher;
    private final QueueProperties queueProperties;

    // Map of channelId -> (userId -> ScheduledFuture)
    private final ConcurrentMap<Long, ConcurrentMap<Long, ScheduledFuture<?>>> scheduledTimeouts = new ConcurrentHashMap<>();

    public QueueInactivityTimeoutScheduler(TaskScheduler taskScheduler,
                                           QueueService queueService,
                                           CheckInSessionReader checkInSessionReader,
                                           ApplicationEventPublisher eventPublisher,
                                           QueueProperties queueProperties) {
        this.taskScheduler = taskScheduler;
        this.queueService = queueService;
        this.checkInSessionReader = checkInSessionReader;
        this.eventPublisher = eventPublisher;
        this.queueProperties = queueProperties;
    }

    /**
     * Schedules an inactivity timeout for a user in a channel's queue.
     *
     * @param userId The user ID
     * @param channelId The channel ID
     */
    public void scheduleTimeout(long userId, long channelId) {
        // Cancel any existing timeout for this user in this channel
        cancelTimeout(userId, channelId);

        long timeoutMillis = queueProperties.getUserTimeoutUnit()
                .toMillis(queueProperties.getUserTimeoutLength());
        Instant timeoutInstant = Instant.now().plusMillis(timeoutMillis);

        ScheduledFuture<?> future = taskScheduler.schedule(
                () -> handleTimeout(userId, channelId),
                timeoutInstant
        );

        scheduledTimeouts
                .computeIfAbsent(channelId, _ -> new ConcurrentHashMap<>())
                .put(userId, future);

        LOGGER.debug("Scheduled queue inactivity timeout for user {} in channel {} at {}",
                userId, channelId, timeoutInstant);
    }

    /**
     * Cancels the inactivity timeout for a user in a channel.
     *
     * @param userId The user ID
     * @param channelId The channel ID
     * @return true if a timeout was cancelled, false if none was scheduled
     */
    public boolean cancelTimeout(long userId, long channelId) {
        ConcurrentMap<Long, ScheduledFuture<?>> channelTimeouts = scheduledTimeouts.get(channelId);
        if (channelTimeouts == null) {
            return false;
        }

        ScheduledFuture<?> future = channelTimeouts.remove(userId);
        if (future != null) {
            boolean cancelled = future.cancel(false);
            LOGGER.debug("Cancelled queue inactivity timeout for user {} in channel {}: {}",
                    userId, channelId, cancelled);
            return cancelled;
        }
        return false;
    }

    /**
     * Cancels all inactivity timeouts for a channel.
     * Called when a check-in session starts (queue is full).
     *
     * @param channelId The channel ID
     */
    public void cancelAllTimeoutsForChannel(long channelId) {
        ConcurrentMap<Long, ScheduledFuture<?>> channelTimeouts = scheduledTimeouts.remove(channelId);
        if (channelTimeouts != null) {
            int count = 0;
            for (ScheduledFuture<?> future : channelTimeouts.values()) {
                future.cancel(false);
                count++;
            }
            LOGGER.info("Cancelled {} queue inactivity timeouts for channel {}", count, channelId);
        }
    }

    /**
     * Handles the timeout when it fires.
     */
    private void handleTimeout(long userId, long channelId) {
        // Remove from tracking map first
        ConcurrentMap<Long, ScheduledFuture<?>> channelTimeouts = scheduledTimeouts.get(channelId);
        if (channelTimeouts != null) {
            channelTimeouts.remove(userId);
        }

        // Check if there's an active check-in session - if so, don't remove the user
        // The user is participating in check-in and shouldn't be timed out
        if (checkInSessionReader.hasActiveSession(channelId)) {
            LOGGER.debug("Queue inactivity timeout for user {} in channel {} skipped - check-in session is active",
                    userId, channelId);
            return;
        }

        // Check if user is still in the queue
        Set<Long> currentQueue = queueService.showQueue(channelId);
        if (!currentQueue.contains(userId)) {
            LOGGER.debug("Queue inactivity timeout for user {} in channel {} skipped - user no longer in queue",
                    userId, channelId);
            return;
        }

        // Remove the user from the queue
        QueueOutcome outcome = queueService.leaveQueue(userId, channelId);
        if (outcome == QueueOutcome.DEQUEUED) {
            LOGGER.info("User {} removed from queue in channel {} due to inactivity timeout", userId, channelId);

            // Get updated queue snapshot
            Set<Long> remainingQueue = queueService.showQueue(channelId);
            int maxQueueSize = queueService.showMaxQueueSize(channelId);

            // Publish event for notification
            eventPublisher.publishEvent(new PlayerQueueTimeoutEvent(
                    userId,
                    channelId,
                    remainingQueue,
                    maxQueueSize
            ));
        }
    }

    /**
     * Checks if a timeout is scheduled for a user in a channel.
     *
     * @param userId The user ID
     * @param channelId The channel ID
     * @return true if a timeout is scheduled and not yet executed
     */
    public boolean hasScheduledTimeout(long userId, long channelId) {
        ConcurrentMap<Long, ScheduledFuture<?>> channelTimeouts = scheduledTimeouts.get(channelId);
        if (channelTimeouts == null) {
            return false;
        }
        ScheduledFuture<?> future = channelTimeouts.get(userId);
        return future != null && !future.isDone();
    }
}

