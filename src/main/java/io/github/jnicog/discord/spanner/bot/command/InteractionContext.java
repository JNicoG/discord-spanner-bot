package io.github.jnicog.discord.spanner.bot.command;

import java.time.OffsetDateTime;

public interface InteractionContext {
    OffsetDateTime eventTime();
    String commandName(); // Or componentId for buttons
    long userId();
    long channelId();
    InteractionResponder interactionResponder();
}
