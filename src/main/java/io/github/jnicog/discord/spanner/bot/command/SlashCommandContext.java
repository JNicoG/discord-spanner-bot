package io.github.jnicog.discord.spanner.bot.command;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * Does not contain any responder - responses are returned from handlers instead.
 */
public record SlashCommandContext(
        OffsetDateTime eventTime,
        String commandName,
        long userId,
        long channelId,
        Map<String, String> options
) {

    public SlashCommandContext {
        Objects.requireNonNull(eventTime);
        Objects.requireNonNull(commandName);
        Objects.requireNonNull(options);
    }
}
