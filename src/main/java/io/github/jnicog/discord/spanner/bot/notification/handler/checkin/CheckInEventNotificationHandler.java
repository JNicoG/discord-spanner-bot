package io.github.jnicog.discord.spanner.bot.notification.handler.checkin;

import io.github.jnicog.discord.spanner.bot.checkin.CheckInService;
import io.github.jnicog.discord.spanner.bot.config.QueueProperties;
import io.github.jnicog.discord.spanner.bot.event.checkin.PlayerCheckInEvent;
import io.github.jnicog.discord.spanner.bot.notification.CheckInMessageGateway;
import io.github.jnicog.discord.spanner.bot.notification.handler.CommandEventNotificationHandler;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CheckInEventNotificationHandler implements CommandEventNotificationHandler<PlayerCheckInEvent> {

    private final QueueProperties queueProperties;
    private final CheckInMessageGateway checkInMessageGateway;

    private static final Emoji CHECKED_IN_SYMBOL = Emoji.fromUnicode("U+2714");
    private static final String NOT_CHECKED_IN_SYMBOL = "X";

    public CheckInEventNotificationHandler(QueueProperties queueProperties,
                                           CheckInMessageGateway checkInMessageGateway) {
        this.queueProperties = queueProperties;
        this.checkInMessageGateway = checkInMessageGateway;
    }

    @Override
    public void handle(PlayerCheckInEvent event) {

        Map<Long, Boolean> checkInStatusSnapshot = event.getUpdatedCheckInSnapshot();

        // Deduplicate with CheckInStartedEventNotificationHandler
        String playerStatusList = checkInStatusSnapshot.entrySet().stream()
                .map(entry -> String.format("<@%d> [%s]", entry.getKey(), entry.getValue()
                        ? CHECKED_IN_SYMBOL : NOT_CHECKED_IN_SYMBOL))
                .collect(Collectors.joining(" | "));

        String message = String.format(
                "The queue has been filled!\n" +
                        "Click the %s button within %s %s to accept.\n" +
                        "Waiting for all players to accept...\n" +
                        "%s",
                CHECKED_IN_SYMBOL,
                queueProperties.getCheckInTimeoutLength(),
                queueProperties.getCheckInTimeoutLength() == 1 ?
                        StringUtils.chop(queueProperties.getCheckInTimeoutUnit().toString().toLowerCase())
                        : queueProperties.getCheckInTimeoutUnit().toString().toLowerCase(),
                playerStatusList
        );
        // Deduplicate with CheckInStartedEventNotificationHandler

        checkInMessageGateway.updateCheckInMessage(
                event.getContext().channelId(),
                event.getCheckInMessageId(),
                message
        );
    }
}
