package io.github.jnicog.discord.spanner.bot.checkin;

import io.github.jnicog.discord.spanner.bot.event.queue.PlayerJoinedQueueEventV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
public class StartCheckInTrigger {

    private static final Logger LOGGER = LoggerFactory.getLogger(StartCheckInTrigger.class);

    private final CheckInService checkInService;

    public StartCheckInTrigger(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    @EventListener
    @Order(2)
    public void onQueueInteractionEvent(PlayerJoinedQueueEventV2 event) {
        boolean justFilledQueue = event.getUpdatedQueueSnapshot().size() == event.getMaxQueueSize();

        if (justFilledQueue) {
            LOGGER.info("Queue for channel {} filled. Triggering check-in event.", event.getContext().channelId());
            checkInService.startCheckIn(event.getContext().channelId(), event.getUpdatedQueueSnapshot());
        }

    }

}
