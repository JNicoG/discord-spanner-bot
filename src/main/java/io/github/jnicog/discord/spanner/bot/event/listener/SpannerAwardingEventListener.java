package io.github.jnicog.discord.spanner.bot.event.listener;

import io.github.jnicog.discord.spanner.bot.event.checkin.CheckInCancelledEvent;
import io.github.jnicog.discord.spanner.bot.event.checkin.CheckInTimeoutEvent;
import io.github.jnicog.discord.spanner.bot.event.checkin.UnkeenDuringCheckInEvent;
import io.github.jnicog.discord.spanner.bot.event.queue.PlayerLeftQueueEvent;
import io.github.jnicog.discord.spanner.bot.spanner.SpannerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Listener that awards spanners when users perform spanner-worthy actions.
 *
 * <p>Spanners are awarded when:</p>
 * <ul>
 *   <li>A user leaves the queue via /unkeen (during check-in or otherwise)</li>
 *   <li>A user cancels the check-in session via the spanner button</li>
 *   <li>A user does not check-in on time (timeout)</li>
 * </ul>
 */
@Component
public class SpannerAwardingEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpannerAwardingEventListener.class);

    private final SpannerService spannerService;

    public SpannerAwardingEventListener(SpannerService spannerService) {
        this.spannerService = spannerService;
    }

    /**
     * Awards a spanner when a user leaves the queue via /unkeen.
     * Only awards if a check-in session is active.
     */
    @EventListener
    @Order(10)
    public void onPlayerLeftQueue(PlayerLeftQueueEvent event) {
        if (!event.isCheckInSessionActive()) {
            LOGGER.debug("Player {} left queue in channel {} but no check-in active, no spanner awarded",
                    event.getContext().userId(), event.getContext().channelId());
            return;
        }

        long userId = event.getTargetUserId();
        long channelId = event.getTargetChannelId();

        LOGGER.info("Awarding spanner to user {} in channel {} for leaving queue during check-in",
                userId, channelId);
        spannerService.incrementSpannerCount(userId, channelId);
    }

    /**
     * Awards a spanner when a user cancels the check-in session via /unkeen during check-in.
     */
    @EventListener
    @Order(10)
    public void onUnkeenDuringCheckIn(UnkeenDuringCheckInEvent event) {
        long userId = event.getTargetUserId();
        long channelId = event.getTargetChannelId();

        LOGGER.info("Awarding spanner to user {} in channel {} for cancelling check-in via /unkeen",
                userId, channelId);
        spannerService.incrementSpannerCount(userId, channelId);
    }

    /**
     * Awards a spanner when a user cancels the check-in session via the spanner button.
     */
    @EventListener
    @Order(10)
    public void onCheckInCancelled(CheckInCancelledEvent event) {
        long userId = event.getTargetUserId();
        long channelId = event.getTargetChannelId();

        LOGGER.info("Awarding spanner to user {} in channel {} for cancelling check-in via button",
                userId, channelId);
        spannerService.incrementSpannerCount(userId, channelId);
    }

    /**
     * Awards spanners to all users who did not check-in on time.
     */
    @EventListener
    @Order(10)
    public void onCheckInTimeout(CheckInTimeoutEvent event) {
        long channelId = event.getChannelId();

        for (Long userId : event.getUsersWhoDidNotCheckIn()) {
            LOGGER.info("Awarding spanner to user {} in channel {} for not checking in on time",
                    userId, channelId);
            spannerService.incrementSpannerCount(userId, channelId);
        }
    }
}

