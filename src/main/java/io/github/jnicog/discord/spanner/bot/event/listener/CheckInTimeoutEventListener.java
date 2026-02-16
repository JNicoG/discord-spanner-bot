package io.github.jnicog.discord.spanner.bot.event.listener;

import io.github.jnicog.discord.spanner.bot.checkin.CheckInTimeoutScheduler;
import io.github.jnicog.discord.spanner.bot.event.checkin.CheckInCancelledEvent;
import io.github.jnicog.discord.spanner.bot.event.checkin.CheckInCompletedEvent;
import io.github.jnicog.discord.spanner.bot.event.checkin.CheckInStartedEvent;
import io.github.jnicog.discord.spanner.bot.event.checkin.UnkeenDuringCheckInEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Listener that manages check-in timeout scheduling.
 *
 * <p>Schedules timeouts when check-in sessions start and cancels them
 * when sessions complete or are cancelled.</p>
 */
@Component
public class CheckInTimeoutEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckInTimeoutEventListener.class);

    private final CheckInTimeoutScheduler timeoutScheduler;

    public CheckInTimeoutEventListener(CheckInTimeoutScheduler timeoutScheduler) {
        this.timeoutScheduler = timeoutScheduler;
    }

    /**
     * Schedule a timeout when a check-in session starts.
     */
    @EventListener
    @Order(1) // Run early to ensure timeout is scheduled
    public void onCheckInStarted(CheckInStartedEvent event) {
        long channelId = event.getSession().getChannelId();
        LOGGER.debug("Check-in started for channel {}, scheduling timeout", channelId);
        timeoutScheduler.scheduleTimeout(channelId);
    }

    /**
     * Cancel the timeout when all players have checked in.
     */
    @EventListener
    @Order(1)
    public void onCheckInCompleted(CheckInCompletedEvent event) {
        long channelId = event.getContext().channelId();
        LOGGER.debug("Check-in completed for channel {}, cancelling timeout", channelId);
        timeoutScheduler.cancelTimeout(channelId);
    }

    /**
     * Cancel the timeout when check-in is cancelled via button.
     */
    @EventListener
    @Order(1)
    public void onCheckInCancelled(CheckInCancelledEvent event) {
        long channelId = event.getContext().channelId();
        LOGGER.debug("Check-in cancelled for channel {}, cancelling timeout", channelId);
        timeoutScheduler.cancelTimeout(channelId);
    }

    /**
     * Cancel the timeout when check-in is cancelled via /unkeen.
     */
    @EventListener
    @Order(1)
    public void onUnkeenDuringCheckIn(UnkeenDuringCheckInEvent event) {
        long channelId = event.getContext().channelId();
        LOGGER.debug("Check-in cancelled via /unkeen for channel {}, cancelling timeout", channelId);
        timeoutScheduler.cancelTimeout(channelId);
    }
}

