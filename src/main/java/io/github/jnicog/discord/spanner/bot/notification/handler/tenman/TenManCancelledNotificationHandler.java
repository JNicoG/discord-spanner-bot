package io.github.jnicog.discord.spanner.bot.notification.handler.tenman;

import io.github.jnicog.discord.spanner.bot.event.tenman.TenManPollCancelledEvent;
import io.github.jnicog.discord.spanner.bot.notification.TenManMessageGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class TenManCancelledNotificationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TenManCancelledNotificationHandler.class);

    private final TenManMessageGateway gateway;

    public TenManCancelledNotificationHandler(TenManMessageGateway gateway) {
        this.gateway = gateway;
    }

    @EventListener
    public void onPollCancelled(TenManPollCancelledEvent event) {
        if (event.getDiscordMessageId() == null) {
            LOGGER.debug("No Discord message ID for cancelled poll in channel {} — nothing to delete", event.getChannelId());
            return;
        }
        gateway.deleteMessage(event.getChannelId(), event.getDiscordMessageId());
    }
}
