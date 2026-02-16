package io.github.jnicog.discord.spanner.bot.command;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * JDA-decoupled context for button interactions.
 * Does not contain any responder - responses are returned from handlers instead.
 */
public record ButtonInteractionContext(
        OffsetDateTime eventTime,
        String componentId,
        long userId,
        long channelId,
        long messageId
) {

    public ButtonInteractionContext {
        Objects.requireNonNull(eventTime);
        Objects.requireNonNull(componentId);
    }
}
