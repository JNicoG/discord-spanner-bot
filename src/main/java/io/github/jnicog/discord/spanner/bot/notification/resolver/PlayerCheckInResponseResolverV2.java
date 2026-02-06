package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolverV2;
import io.github.jnicog.discord.spanner.bot.config.QueueProperties;
import io.github.jnicog.discord.spanner.bot.event.checkin.PlayerCheckInEventV2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * V2 ResponseResolver for PlayerCheckInEventV2.
 * Updates the check-in message with the current check-in status.
 */
@Component
public class PlayerCheckInResponseResolverV2 implements ResponseResolverV2<PlayerCheckInEventV2> {

    private static final String CHECKED_IN_SYMBOL = "✔"; // Unicode checkmark
    private static final String NOT_CHECKED_IN_SYMBOL = "X";

    private final QueueProperties queueProperties;

    public PlayerCheckInResponseResolverV2(QueueProperties queueProperties) {
        this.queueProperties = queueProperties;
    }

    @Override
    public InteractionResponse resolve(PlayerCheckInEventV2 event) {
        Map<Long, Boolean> checkInStatusSnapshot = event.getUpdatedCheckInSnapshot();

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

        return new InteractionResponse.UpdateOriginalMessage(message);
    }
}

