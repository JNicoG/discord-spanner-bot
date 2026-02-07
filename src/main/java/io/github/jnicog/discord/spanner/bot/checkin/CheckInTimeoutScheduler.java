package io.github.jnicog.discord.spanner.bot.checkin;

import io.github.jnicog.discord.spanner.bot.config.QueueProperties;
import io.github.jnicog.discord.spanner.bot.event.checkin.CheckInTimeoutEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Manages scheduling and cancellation of check-in timeout tasks.
 * 
 * <p>This component is responsible for:</p>
 * <ul>
 *   <li>Scheduling timeout tasks when a check-in session starts</li>
 *   <li>Cancelling timeout tasks when a session completes or is cancelled</li>
 *   <li>Publishing {@link CheckInTimeoutEvent} when a timeout occurs</li>
 * </ul>
 * 
 * <p>Timeout tasks are stored in a ConcurrentMap keyed by channel ID.
 * When a session ends (completed, cancelled, or timed out), the task is removed.</p>
 */
@Component
public class CheckInTimeoutScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckInTimeoutScheduler.class);

    private final TaskScheduler taskScheduler;
    private final CheckInService checkInService;
    private final ApplicationEventPublisher eventPublisher;
    private final QueueProperties queueProperties;

    private final ConcurrentMap<Long, ScheduledFuture<?>> scheduledTimeouts = new ConcurrentHashMap<>();

    public CheckInTimeoutScheduler(TaskScheduler taskScheduler,
                                   CheckInService checkInService,
                                   ApplicationEventPublisher eventPublisher,
                                   QueueProperties queueProperties) {
        this.taskScheduler = taskScheduler;
        this.checkInService = checkInService;
        this.eventPublisher = eventPublisher;
        this.queueProperties = queueProperties;
    }

    /**
     * Schedules a timeout task for the given channel's check-in session.
     * If a timeout is already scheduled for this channel, it will be replaced.
     *
     * @param channelId The channel ID for the check-in session
     */
    public void scheduleTimeout(long channelId) {
        // Cancel any existing timeout for this channel
        cancelTimeout(channelId);

        long timeoutMillis = queueProperties.getCheckInTimeoutUnit()
                .toMillis(queueProperties.getCheckInTimeoutLength());
        Instant timeoutInstant = Instant.now().plusMillis(timeoutMillis);

        ScheduledFuture<?> future = taskScheduler.schedule(
                () -> handleTimeout(channelId),
                timeoutInstant
        );

        scheduledTimeouts.put(channelId, future);
        LOGGER.info("Scheduled check-in timeout for channel {} at {}", channelId, timeoutInstant);
    }

    /**
     * Cancels the timeout task for the given channel, if one exists.
     * Called when a session completes or is cancelled before timeout.
     *
     * @param channelId The channel ID
     * @return true if a timeout was cancelled, false if none was scheduled
     */
    public boolean cancelTimeout(long channelId) {
        ScheduledFuture<?> future = scheduledTimeouts.remove(channelId);
        if (future != null) {
            boolean cancelled = future.cancel(false); // Don't interrupt if running
            LOGGER.debug("Cancelled check-in timeout for channel {}: {}", channelId, cancelled);
            return cancelled;
        }
        return false;
    }

    /**
     * Handles the timeout when it fires.
     * Atomically removes the session and publishes a timeout event.
     */
    private void handleTimeout(long channelId) {
        // Remove from our tracking map first
        scheduledTimeouts.remove(channelId);

        LOGGER.info("Check-in timeout triggered for channel {}", channelId);

        // Get timeout result from the service (which removes the session)
        CheckInTimeoutResult result = checkInService.timeoutSession(channelId);

        if (result.result() == CheckInAttemptResult.SESSION_TIMED_OUT) {
            LOGGER.info("Check-in session timed out for channel {}. Users who didn't check in: {}",
                    channelId, result.usersWhoDidNotCheckIn());

            // Publish event for notification handling
            eventPublisher.publishEvent(new CheckInTimeoutEvent(result));
        } else {
            LOGGER.debug("Timeout fired for channel {} but session was already gone (result: {})",
                    channelId, result.result());
        }
    }

    /**
     * Checks if a timeout is scheduled for the given channel.
     *
     * @param channelId The channel ID
     * @return true if a timeout is scheduled and not yet executed
     */
    public boolean hasScheduledTimeout(long channelId) {
        ScheduledFuture<?> future = scheduledTimeouts.get(channelId);
        return future != null && !future.isDone();
    }
}
