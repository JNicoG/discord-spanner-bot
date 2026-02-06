package io.github.jnicog.discord.spanner.bot.command;

import java.time.OffsetDateTime;

/**
 * V1 interaction context that couples JDA response capability with domain data.
 *
 * @deprecated Use V2 JDA-free contexts instead:
 * @see SlashCommandContext
 * @see ButtonInteractionContext
 */
@Deprecated
public interface InteractionContext {
    OffsetDateTime eventTime();
    String commandName();
    long userId();
    long channelId();
    InteractionResponder interactionResponder();
}
