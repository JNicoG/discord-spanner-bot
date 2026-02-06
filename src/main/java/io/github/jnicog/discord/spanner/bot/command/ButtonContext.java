package io.github.jnicog.discord.spanner.bot.command;

import java.time.OffsetDateTime;
import java.util.Objects;

public record ButtonContext(
        OffsetDateTime eventTime,
        String componentId,
        long userId,
        long channelId,
        InteractionResponder interactionResponder,
        long messageId
) implements InteractionContext {

    public ButtonContext {
        Objects.requireNonNull(eventTime);
        Objects.requireNonNull(componentId);
        Objects.requireNonNull(interactionResponder);
    }

    @Override
    public String commandName() {
        return componentId;
    }
}
