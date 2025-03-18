package io.github.jnicog.discord.spanner.bot.service;

import io.github.jnicog.discord.spanner.bot.event.CheckInAcceptEvent;
import io.github.jnicog.discord.spanner.bot.event.CheckInAlreadyAcceptedEvent;
import io.github.jnicog.discord.spanner.bot.event.CheckInCancelledEvent;
import io.github.jnicog.discord.spanner.bot.event.CheckInCompletedEvent;
import io.github.jnicog.discord.spanner.bot.event.CheckInOutdatedEvent;
import io.github.jnicog.discord.spanner.bot.event.CheckInStartedEvent;
import io.github.jnicog.discord.spanner.bot.event.CheckInTimeoutEvent;
import io.github.jnicog.discord.spanner.bot.event.NonMemberInteractionEvent;
import io.github.jnicog.discord.spanner.bot.event.PlayerTimeoutEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class QueueEventPublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueueEventPublisher.class);

    private final ApplicationEventPublisher eventPublisher;

    public QueueEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void publishCheckInAcceptEvent(CheckInAcceptEvent event) {
        LOGGER.debug("Publishing CheckInAcceptEvent in channel {} by user {}",
                event.getChannelId(), event.getUser().getName());
        eventPublisher.publishEvent(event);
    }

    public void publishCheckInAlreadyAcceptedEvent(CheckInAlreadyAcceptedEvent event) {
        LOGGER.debug("Publishing CheckInAlreadyAcceptedEvent in channel {} by user {}",
                event.getChannelId(), event.getButtonInteractionEvent().getUser().getName());
        eventPublisher.publishEvent(event);
    }

    public void publishCheckInCancelledEvent(CheckInCancelledEvent event) {
        LOGGER.debug("Publishing CheckInCancelledEvent in channel {} by user {}",
                event.getChannelId(), event.getUser().getName());
        eventPublisher.publishEvent(event);
    }

    public void publishCheckInOutdatedEvent(CheckInOutdatedEvent event) {
        LOGGER.debug("Publishing CheckInOutdatedEvent in channel {} by user {}",
                event.getChannelId(), event.getButtonInteractionEvent().getUser().getName());
        eventPublisher.publishEvent(event);
    }

    public void publishCheckInCompletedEvent(CheckInCompletedEvent event) {
        LOGGER.debug("Publishing CheckInCompletedEvent for channel {}", event.getChannelId());
        eventPublisher.publishEvent(event);
    }

    public void publishCheckInStartedEvent(CheckInStartedEvent event) {
        LOGGER.debug("Publishing CheckInStartedEvent for channel {}", event.getChannelId());
        eventPublisher.publishEvent(event);
    }

    public void publishCheckInTimeoutEvent(CheckInTimeoutEvent event) {
        LOGGER.debug("Publishing CheckInTimeoutEvent for channel {}, {} users didn't check in",
                event.getChannelId(), event.getNotCheckedInUsers().size());
        eventPublisher.publishEvent(event);
    }

    public void publishPlayerTimeoutEvent(PlayerTimeoutEvent event) {
        LOGGER.debug("Publishing PlayerTimeoutEvent for user {} in channel {}",
                event.getUser().getName(), event.getChannelId());
        eventPublisher.publishEvent(event);
    }

    public void publishNonMemberInteractionEvent(NonMemberInteractionEvent event) {
        LOGGER.debug("Publishing NonMemberInteractionEvent for user {} in channel {}",
                event.getButtonInteractionEvent().getUser().getName(), event.getChannelId());
        eventPublisher.publishEvent(event);
    }

}
