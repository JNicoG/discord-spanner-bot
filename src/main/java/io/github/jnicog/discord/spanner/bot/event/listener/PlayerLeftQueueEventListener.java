package io.github.jnicog.discord.spanner.bot.event.listener;

import io.github.jnicog.discord.spanner.bot.checkin.CheckInService;
import io.github.jnicog.discord.spanner.bot.event.checkin.PlayerCancelledCheckInEvent;
import io.github.jnicog.discord.spanner.bot.event.queue.PlayerLeftQueueEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
public class PlayerLeftQueueEventListener {

    private final ApplicationEventPublisher eventPublisher;
    private final CheckInService checkInService;

    public PlayerLeftQueueEventListener(ApplicationEventPublisher eventPublisher, CheckInService checkInService) {
        this.eventPublisher = eventPublisher;
        this.checkInService = checkInService;
    }

    @EventListener
    @Order(2)
    public void onPlayerLeftQueueEvent(PlayerLeftQueueEvent event) {
        long userId = event.getContext().userId();
        long channelId = event.getContext().channelId();

        if (!checkInService.hasActiveSession(channelId)) {
            return;
        }


    }

}
