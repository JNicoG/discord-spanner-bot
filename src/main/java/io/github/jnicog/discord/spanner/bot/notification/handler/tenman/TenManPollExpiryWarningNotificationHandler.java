package io.github.jnicog.discord.spanner.bot.notification.handler.tenman;

import io.github.jnicog.discord.spanner.bot.event.tenman.TenManPollExpiryWarningEvent;
import io.github.jnicog.discord.spanner.bot.notification.TenManMessageGateway;
import io.github.jnicog.discord.spanner.bot.tenman.TenManPollSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class TenManPollExpiryWarningNotificationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TenManPollExpiryWarningNotificationHandler.class);

    private final TenManMessageGateway gateway;

    public TenManPollExpiryWarningNotificationHandler(TenManMessageGateway gateway) {
        this.gateway = gateway;
    }

    @EventListener
    public void onPollClosingSoon(TenManPollExpiryWarningEvent event) {
        TenManPollSnapshot snapshot = event.getSnapshot();
        LOGGER.info("Sending expiry warning for ten-man poll {} in channel {}", snapshot.pollId(), snapshot.channelId());
        gateway.sendNotification(snapshot.channelId(),
                "⏰  The ten-man sign-up poll closes <t:" + snapshot.closesAt().toEpochSecond() + ":R>! " +
                "If you haven't signed up yet, now's your chance!");
    }
}
