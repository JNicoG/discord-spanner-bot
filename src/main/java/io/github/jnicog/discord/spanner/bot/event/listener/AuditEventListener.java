package io.github.jnicog.discord.spanner.bot.event.listener;

import io.github.jnicog.discord.spanner.bot.audit.AuditService;
import io.github.jnicog.discord.spanner.bot.event.checkin.CheckInCancelledEvent;
import io.github.jnicog.discord.spanner.bot.event.checkin.CheckInCompletedEvent;
import io.github.jnicog.discord.spanner.bot.event.checkin.CheckInStartedEvent;
import io.github.jnicog.discord.spanner.bot.event.checkin.CheckInTimeoutEvent;
import io.github.jnicog.discord.spanner.bot.event.checkin.PlayerCheckInEvent;
import io.github.jnicog.discord.spanner.bot.event.checkin.UnkeenDuringCheckInEvent;
import io.github.jnicog.discord.spanner.bot.event.queue.PlayerJoinedQueueEvent;
import io.github.jnicog.discord.spanner.bot.event.queue.PlayerLeftQueueEvent;
import io.github.jnicog.discord.spanner.bot.event.queue.PlayerQueueTimeoutEvent;
import io.github.jnicog.discord.spanner.bot.repository.entity.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Listener that records audit events for all significant actions.
 * Uses @Order(100) to run after main business logic.
 */
@Component
public class AuditEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditEventListener.class);

    private final AuditService auditService;

    public AuditEventListener(AuditService auditService) {
        this.auditService = auditService;
    }

    // ==================== Queue Events ====================

    @EventListener
    @Order(100)
    public void onPlayerJoinedQueue(PlayerJoinedQueueEvent event) {
        Map<String, Object> data = new HashMap<>();
        data.put("queue_size_after", event.getUpdatedQueueSnapshot().size());
        data.put("max_queue_size", event.getMaxQueueSize());

        auditService.recordEvent(
                event.getContext().channelId(),
                event.getContext().userId(),
                EventType.PLAYER_JOINED_QUEUE,
                data
        );
    }

    @EventListener
    @Order(100)
    public void onPlayerLeftQueue(PlayerLeftQueueEvent event) {
        Map<String, Object> data = new HashMap<>();
        data.put("queue_size_after", event.getUpdatedQueueSnapshot().size());
        data.put("max_queue_size", event.getMaxQueueSize());
        data.put("check_in_active", event.isCheckInSessionActive());

        auditService.recordEvent(
                event.getContext().channelId(),
                event.getContext().userId(),
                EventType.PLAYER_LEFT_QUEUE,
                data
        );
    }

    @EventListener
    @Order(100)
    public void onPlayerQueueTimeout(PlayerQueueTimeoutEvent event) {
        Map<String, Object> data = new HashMap<>();
        data.put("remaining_queue_size", event.getRemainingQueueSnapshot().size());
        data.put("max_queue_size", event.getMaxQueueSize());

        auditService.recordEvent(
                event.getChannelId(),
                event.getUserId(),
                EventType.PLAYER_QUEUE_TIMEOUT,
                data
        );
    }

    // ==================== Check-in Events ====================

    @EventListener
    @Order(100)
    public void onCheckInStarted(CheckInStartedEvent event) {
        Map<String, Object> data = new HashMap<>();
        data.put("participants", List.copyOf(event.getSession().getUserCheckInStatusSnapshot().keySet()));

        auditService.recordEvent(
                event.getSession().getChannelId(),
                EventType.CHECK_IN_STARTED,
                data
        );
    }

    @EventListener
    @Order(100)
    public void onPlayerCheckedIn(PlayerCheckInEvent event) {
        Map<String, Object> data = new HashMap<>();
        data.put("checked_in_count", event.getUpdatedCheckInSnapshot().values().stream().filter(v -> v).count());
        data.put("total_participants", event.getUpdatedCheckInSnapshot().size());

        auditService.recordEvent(
                event.getContext().channelId(),
                event.getContext().userId(),
                EventType.PLAYER_CHECKED_IN,
                data
        );
    }

    @EventListener
    @Order(100)
    public void onCheckInCompleted(CheckInCompletedEvent event) {
        Map<String, Object> data = new HashMap<>();
        data.put("participants", List.copyOf(event.getFinalCheckInSnapshot().keySet()));

        auditService.recordEvent(
                event.getContext().channelId(),
                event.getContext().userId(),
                EventType.CHECK_IN_COMPLETED,
                data
        );
    }

    @EventListener
    @Order(100)
    public void onCheckInCancelled(CheckInCancelledEvent event) {
        Map<String, Object> data = new HashMap<>();
        data.put("cancelled_by_user_id", event.getContext().userId());
        data.put("remaining_users", List.copyOf(event.getRemainingUsersForQueue()));
        data.put("reason", "BUTTON");

        auditService.recordEvent(
                event.getContext().channelId(),
                event.getContext().userId(),
                EventType.CHECK_IN_CANCELLED,
                data
        );
    }

    @EventListener
    @Order(100)
    public void onUnkeenDuringCheckIn(UnkeenDuringCheckInEvent event) {
        Map<String, Object> data = new HashMap<>();
        data.put("cancelled_by_user_id", event.getContext().userId());
        data.put("remaining_users", List.copyOf(event.getRemainingUsersInQueue()));
        data.put("reason", "UNKEEN");

        auditService.recordEvent(
                event.getContext().channelId(),
                event.getContext().userId(),
                EventType.CHECK_IN_CANCELLED,
                data
        );
    }

    @EventListener
    @Order(100)
    public void onCheckInTimeout(CheckInTimeoutEvent event) {
        Map<String, Object> data = new HashMap<>();
        data.put("users_who_checked_in", List.copyOf(event.getUsersWhoCheckedIn()));
        data.put("users_who_did_not_check_in", List.copyOf(event.getUsersWhoDidNotCheckIn()));

        auditService.recordEvent(
                event.getChannelId(),
                EventType.CHECK_IN_TIMEOUT,
                data
        );
    }
}

