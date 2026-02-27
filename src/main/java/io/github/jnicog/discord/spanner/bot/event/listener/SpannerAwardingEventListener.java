package io.github.jnicog.discord.spanner.bot.event.listener;

import io.github.jnicog.discord.spanner.bot.audit.AuditService;
import io.github.jnicog.discord.spanner.bot.event.checkin.CheckInCancelledEvent;
import io.github.jnicog.discord.spanner.bot.event.checkin.CheckInTimeoutEvent;
import io.github.jnicog.discord.spanner.bot.event.checkin.UnkeenDuringCheckInEvent;
import io.github.jnicog.discord.spanner.bot.event.queue.PlayerLeftQueueEvent;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManRosterResignedButtonEvent;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManRosterResignedSlashEvent;
import io.github.jnicog.discord.spanner.bot.repository.entity.EventType;
import io.github.jnicog.discord.spanner.bot.spanner.SpannerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Listener that awards spanners when users perform spanner-worthy actions.
 *
 * <p>Spanners are awarded when:</p>
 * <ul>
 *   <li>A user leaves the queue via /unkeen (during check-in)</li>
 *   <li>A user cancels the check-in session via the spanner button</li>
 *   <li>A user does not check-in on time (timeout)</li>
 * </ul>
 */
@Component
public class SpannerAwardingEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpannerAwardingEventListener.class);

    private final SpannerService spannerService;
    private final AuditService auditService;

    public SpannerAwardingEventListener(SpannerService spannerService, AuditService auditService) {
        this.spannerService = spannerService;
        this.auditService = auditService;
    }

    /**
     * Awards a spanner when a user successfully leaves the queue via /unkeen.
     * This is triggered when the user is in the queue but NOT in an active check-in session.
     * (If they're in a check-in session, UnkeenDuringCheckInEvent is published instead)
     */
    @EventListener
    @Order(10)
    public void onPlayerLeftQueue(PlayerLeftQueueEvent event) {
        long userId = event.getTargetUserId();
        long channelId = event.getTargetChannelId();

        LOGGER.info("Awarding spanner to user {} in channel {} for leaving queue via /unkeen",
                userId, channelId);
        spannerService.incrementSpannerCount(userId, channelId);

        recordSpannerAuditEvent(channelId, userId, "LEFT_QUEUE");
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

        recordSpannerAuditEvent(channelId, userId, "UNKEEN_DURING_CHECK_IN");
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

        recordSpannerAuditEvent(channelId, userId, "CHECK_IN_CANCELLED");
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

            recordSpannerAuditEvent(channelId, userId, "CHECK_IN_TIMEOUT");
        }
    }

    @EventListener
    @Order(10)
    public void onTenManRosterResignedByButton(TenManRosterResignedButtonEvent event) {
        awardTenManResignSpanner(event.getUserId(), event.getChannelId());
    }

    @EventListener
    @Order(10)
    public void onTenManRosterResignedBySlash(TenManRosterResignedSlashEvent event) {
        awardTenManResignSpanner(event.getUserId(), event.getChannelId());
    }

    private void awardTenManResignSpanner(long userId, long channelId) {
        LOGGER.info("Awarding spanner to user {} in channel {} for resigning from ten-man roster", userId, channelId);
        spannerService.incrementSpannerCount(userId, channelId);
        recordSpannerAuditEvent(channelId, userId, "TEN_MAN_ROSTER_RESIGNED");
    }

    private void recordSpannerAuditEvent(long channelId, long userId, String reason) {
        Map<String, Object> data = new HashMap<>();
        data.put("reason", reason);
        data.put("new_spanner_count", spannerService.getSpannerCount(userId, channelId));
        auditService.recordEvent(channelId, userId, EventType.SPANNER_AWARDED, data);
    }
}

