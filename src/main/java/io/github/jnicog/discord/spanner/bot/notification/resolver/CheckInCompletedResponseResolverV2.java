package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolverV2;
import io.github.jnicog.discord.spanner.bot.event.checkin.CheckInCompletedEventV2;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * V2 ResponseResolver for CheckInCompletedEventV2.
 * Updates the check-in message to show completion and removes buttons.
 */
@Component
public class CheckInCompletedResponseResolverV2 implements ResponseResolverV2<CheckInCompletedEventV2> {

    private static final String CHECKED_IN_SYMBOL = "✔";

    @Override
    public InteractionResponse resolve(CheckInCompletedEventV2 event) {
        Map<Long, Boolean> checkInStatusSnapshot = event.getFinalCheckInSnapshot();

        // Format the player status list (all should be checked in)
        String playerStatusList = checkInStatusSnapshot.entrySet().stream()
                .map(entry -> String.format("<@%d> [%s]", entry.getKey(),
                        entry.getValue() ? CHECKED_IN_SYMBOL : "X"))
                .collect(Collectors.joining(" | "));

        String message = String.format(
                "All players have checked in.\n%s",
                playerStatusList
        );

        // Update the message and remove buttons
        return new InteractionResponse.UpdateOriginalMessageAndClearComponents(message);
    }
}

