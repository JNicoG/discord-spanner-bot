package io.github.jnicog.discord.spanner.bot.event.listener;

import io.github.jnicog.discord.spanner.bot.checkin.CheckInService;
import io.github.jnicog.discord.spanner.bot.event.queue.PlayerLeftQueueEventV2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @deprecated This listener was intended to handle players leaving the queue during an active
 * check-in session. This functionality is now handled by
 * {@link io.github.jnicog.discord.spanner.bot.command.handler.UnkeenCommandHandlerV2#tryHandleCheckInCancellation}
 * which cancels the session when a participant uses /unkeen during check-in.
 */
@Deprecated
// @Component - Disabled, superseded by UnkeenCommandHandlerV2
public class PlayerLeftQueueEventListener {

    private final ApplicationEventPublisher eventPublisher;
    private final CheckInService checkInService;

    public PlayerLeftQueueEventListener(ApplicationEventPublisher eventPublisher, CheckInService checkInService) {
        this.eventPublisher = eventPublisher;
        this.checkInService = checkInService;
    }

    @EventListener
    @Order(2)
    public void onPlayerLeftQueueEvent(PlayerLeftQueueEventV2 event) {
        long userId = event.getContext().userId();
        long channelId = event.getContext().channelId();

        if (!checkInService.hasActiveSession(channelId)) {
            return;
        }


    }

}
