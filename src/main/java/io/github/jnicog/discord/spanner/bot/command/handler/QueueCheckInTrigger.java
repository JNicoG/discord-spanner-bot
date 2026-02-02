package io.github.jnicog.discord.spanner.bot.command.handler;

import io.github.jnicog.discord.spanner.bot.event.CheckInEvent;
import io.github.jnicog.discord.spanner.bot.event.QueueInteractionEvent;
import io.github.jnicog.discord.spanner.bot.queue.QueueOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
public class QueueCheckInTrigger {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueCheckInTrigger.class);

    private final ApplicationEventPublisher eventPublisher;

    public QueueCheckInTrigger(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    @Order(2)
    public void onQueueFull(QueueInteractionEvent event) {
        boolean justFilledQueue = event.getQueueOutcome() == QueueOutcome.ENQUEUED
                && event.getQueueSnapshot().size() == event.getMaxQueueSize();

        if (justFilledQueue) {
            CheckInEvent checkInEvent = new CheckInEvent(
                    event.getEventTime(),
                    "check-in",
                    event.getUserId(),
                    event.getChannelId(),
                    event.getDetails(),
                    event.getMetadata(),
                    event.getInteractionResponder(),
                    event.getQueueSnapshot(),
                    event.getMaxQueueSize()
            );
            eventPublisher.publishEvent(checkInEvent);
        }

    }

}
