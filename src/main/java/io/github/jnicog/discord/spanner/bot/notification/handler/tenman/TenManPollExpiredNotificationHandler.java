package io.github.jnicog.discord.spanner.bot.notification.handler.tenman;

import io.github.jnicog.discord.spanner.bot.event.tenman.TenManPollExpiredEvent;
import io.github.jnicog.discord.spanner.bot.notification.TenManMessageGateway;
import io.github.jnicog.discord.spanner.bot.tenman.TenManPollSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class TenManPollExpiredNotificationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TenManPollExpiredNotificationHandler.class);
    private static final int CAPACITY = 10;
    private static final int TEST_CAPACITY = 1;

    private final TenManMessageGateway gateway;

    public TenManPollExpiredNotificationHandler(TenManMessageGateway gateway) {
        this.gateway = gateway;
    }

    @EventListener
    public void onPollExpired(TenManPollExpiredEvent event) {
        TenManPollSnapshot snapshot = event.getSnapshot();

        // Locked poll expired — check if the roster was still below capacity (i.e. someone resigned and it wasn't refilled)
        if (snapshot.lockedDateOptionId() != null) {
            int capacity = snapshot.testMode() ? TEST_CAPACITY : CAPACITY;
            boolean rosterStillFull = snapshot.dateOptions().stream()
                    .filter(opt -> opt.id() == snapshot.lockedDateOptionId())
                    .findFirst()
                    .map(opt -> opt.signedUpUserIds().size() >= capacity)
                    .orElse(false);
            if (!rosterStillFull) {
                LOGGER.info("Ten-man poll {} (LOCKED) expired without being refilled — notifying channel", snapshot.pollId());
                gateway.sendNotification(snapshot.channelId(), "The ten-man roster couldn't be filled in time. 😔 The match is off.");
            }
            return;
        }

        int capacity = snapshot.testMode() ? TEST_CAPACITY : CAPACITY;
        boolean anyFull = snapshot.dateOptions().stream()
                .anyMatch(opt -> opt.signedUpUserIds().size() >= capacity);

        if (anyFull) {
            // Match was already announced via TenManDateFullNotificationHandler — nothing to do
            return;
        }

        LOGGER.info("Ten-man poll {} expired with no full dates — deleting message and notifying channel", snapshot.pollId());

        if (snapshot.discordMessageId() != 0) {
            gateway.deleteMessage(snapshot.channelId(), snapshot.discordMessageId());
        }
        gateway.sendNotification(snapshot.channelId(), "No ten mans this time around 😔");
    }
}
