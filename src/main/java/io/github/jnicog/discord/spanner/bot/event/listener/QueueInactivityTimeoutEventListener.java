package io.github.jnicog.discord.spanner.bot.event.listener;

import io.github.jnicog.discord.spanner.bot.event.checkin.CheckInCancelledEvent;
import io.github.jnicog.discord.spanner.bot.event.checkin.CheckInCompletedEvent;
import io.github.jnicog.discord.spanner.bot.event.checkin.CheckInStartedEvent;
import io.github.jnicog.discord.spanner.bot.event.checkin.CheckInTimeoutEvent;
import io.github.jnicog.discord.spanner.bot.event.checkin.UnkeenDuringCheckInEvent;
import io.github.jnicog.discord.spanner.bot.event.queue.PlayerJoinedQueueEvent;
import io.github.jnicog.discord.spanner.bot.event.queue.PlayerLeftQueueEvent;
import io.github.jnicog.discord.spanner.bot.queue.QueueInactivityTimeoutScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Listener that manages queue inactivity timeout scheduling.
 *
 * <p>Schedules timeouts when users join the queue and cancels them
 * when appropriate events occur.</p>
 */
@Component
public class QueueInactivityTimeoutEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueInactivityTimeoutEventListener.class);

    private final QueueInactivityTimeoutScheduler timeoutScheduler;

    public QueueInactivityTimeoutEventListener(QueueInactivityTimeoutScheduler timeoutScheduler) {
        this.timeoutScheduler = timeoutScheduler;
    }

    /**
     * Schedule a timeout when a user joins the queue.
     */
    @EventListener
    @Order(2) // Run after the main queue join logic
    public void onPlayerJoinedQueue(PlayerJoinedQueueEvent event) {
        long userId = event.getContext().userId();
        long channelId = event.getContext().channelId();

        // Only schedule timeout if the queue is NOT full (check-in hasn't started)
        if (event.getUpdatedQueueSnapshot().size() < event.getMaxQueueSize()) {
            LOGGER.debug("Player {} joined queue in channel {}, scheduling inactivity timeout", userId, channelId);
            timeoutScheduler.scheduleTimeout(userId, channelId);
        }
        // If queue is now full, check-in will start and timeouts will be cancelled by onCheckInStarted
    }

    /**
     * Cancel the timeout when a user manually leaves the queue.
     */
    @EventListener
    @Order(1)
    public void onPlayerLeftQueue(PlayerLeftQueueEvent event) {
        long userId = event.getContext().userId();
        long channelId = event.getContext().channelId();
        LOGGER.debug("Player {} left queue in channel {}, cancelling inactivity timeout", userId, channelId);
        timeoutScheduler.cancelTimeout(userId, channelId);
    }

    /**
     * Cancel all timeouts when check-in starts (queue is full).
     * Users in check-in should not be subject to inactivity timeouts.
     */
    @EventListener
    @Order(1)
    public void onCheckInStarted(CheckInStartedEvent event) {
        long channelId = event.getSession().getChannelId();
        LOGGER.debug("Check-in started for channel {}, cancelling all queue inactivity timeouts", channelId);
        timeoutScheduler.cancelAllTimeoutsForChannel(channelId);
    }

    /**
     * When check-in completes successfully, the queue is cleared.
     * No need to reschedule timeouts - users can /keen again to rejoin.
     */
    @EventListener
    @Order(3) // Run after queue is cleared
    public void onCheckInCompleted(CheckInCompletedEvent event) {
        long channelId = event.getContext().channelId();
        LOGGER.debug("Check-in completed for channel {}, no timeout rescheduling needed (queue cleared)", channelId);
        // Queue is cleared on completion, so no users to schedule timeouts for
    }

    /**
     * When check-in is cancelled, remaining users stay in queue.
     * We do NOT reschedule their timeouts as per requirements.
     */
    @EventListener
    @Order(3) // Run after queue updates
    public void onCheckInCancelled(CheckInCancelledEvent event) {
        long channelId = event.getContext().channelId();
        LOGGER.debug("Check-in cancelled for channel {}, NOT rescheduling inactivity timeouts for remaining users", channelId);
        // Intentionally not rescheduling timeouts for users who remain in queue
    }

    /**
     * When check-in times out, users who checked in stay in queue.
     * We do NOT reschedule their timeouts as per requirements.
     */
    @EventListener
    @Order(3) // Run after queue updates
    public void onCheckInTimeout(CheckInTimeoutEvent event) {
        long channelId = event.getChannelId();
        LOGGER.debug("Check-in timed out for channel {}, NOT rescheduling inactivity timeouts for remaining users", channelId);
        // Intentionally not rescheduling timeouts for users who remain in queue
    }

    /**
     * When check-in is cancelled via /unkeen, remaining users stay in queue.
     * We do NOT reschedule their timeouts as per requirements.
     */
    @EventListener
    @Order(3) // Run after queue updates
    public void onUnkeenDuringCheckIn(UnkeenDuringCheckInEvent event) {
        long channelId = event.getContext().channelId();
        LOGGER.debug("Check-in cancelled via /unkeen for channel {}, NOT rescheduling inactivity timeouts for remaining users", channelId);
        // Intentionally not rescheduling timeouts for users who remain in queue
    }
}



