package io.github.jnicog.discord.spanner.bot.checkin;

import io.github.jnicog.discord.spanner.bot.event.queue.PlayerJoinedQueueEventV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Listens for queue events and triggers check-in when the queue is filled.
 *
 * <p>This trigger uses the {@link CheckInSessionManager} interface (via {@link CheckInService})
 * to start check-in sessions. It only depends on the session management capability,
 * demonstrating the Interface Segregation Principle.</p>
 */
@Component
public class StartCheckInTrigger {

    private static final Logger LOGGER = LoggerFactory.getLogger(StartCheckInTrigger.class);

    private final CheckInSessionManager checkInSessionManager;

    public StartCheckInTrigger(CheckInSessionManager checkInSessionManager) {
        this.checkInSessionManager = checkInSessionManager;
    }

    @EventListener
    @Order(2)
    public void onQueueFilled(PlayerJoinedQueueEventV2 event) {
        if (!isQueueJustFilled(event)) {
            return;
        }

        LOGGER.info("Queue for channel {} filled. Triggering check-in session.",
                event.getContext().channelId());
        checkInSessionManager.startCheckIn(
                event.getContext().channelId(),
                event.getUpdatedQueueSnapshot()
        );
    }

    private boolean isQueueJustFilled(PlayerJoinedQueueEventV2 event) {
        return event.getUpdatedQueueSnapshot().size() == event.getMaxQueueSize();
    }
}
