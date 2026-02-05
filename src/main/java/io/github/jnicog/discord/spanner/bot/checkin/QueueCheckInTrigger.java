package io.github.jnicog.discord.spanner.bot.checkin;

import io.github.jnicog.discord.spanner.bot.event.queue.PlayerJoinedQueueEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
public class QueueCheckInTrigger {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueCheckInTrigger.class);

    private final CheckInService checkInService;

    public QueueCheckInTrigger(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    @EventListener
    @Order(2)
    public void onQueueInteractionEvent(PlayerJoinedQueueEvent event) {
        boolean justFilledQueue = event.getUpdatedQueueSnapshot().size() == event.getMaxQueueSize();

        if (justFilledQueue) {
            LOGGER.info("Queue for channel {} filled. Triggering check-in event.", event.getContext().channelId());

            // Perhaps we publish a StartCheckInEvent instead of directly calling service
            // since notification service also needs to know when check-in starts.
            checkInService.startCheckIn(event.getContext().channelId(), event.getUpdatedQueueSnapshot());
        }

    }

    /**
     * Queue check-in sequence:
     * 1. Player issues /keen command and is added to queue.
     * 2. If queue reaches max capacity, start check-in session.
     * 3. A notification is sent to the channel announcing the start of check-in session.
     * 4. Await successful publish of message to channel before starting check-in timer.
     * 5. Await successful publish of message before editing message with buttons for checking in and cancelling.
     */

}
