package io.github.jnicog.discord.spanner.bot.command;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;

public record CommandContext(
        OffsetDateTime eventTime,
        String commandName,
        long userId,
        long channelId,
        InteractionResponder interactionResponder,
        Map<String, String> options
) implements InteractionContext {
    public CommandContext {
        Objects.requireNonNull(eventTime, "eventTime");
        Objects.requireNonNull(commandName, "commandName");
        Objects.requireNonNull(interactionResponder, "interactionResponder");
    }
}
