package io.github.jnicog.discord.spanner.bot.command.handler;

import io.github.jnicog.discord.spanner.bot.command.ButtonInteractionContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResultV2;

import java.util.List;

/**
 * V2 handler interface that receives JDA-free context.
 * No responder is passed - responses are handled by the dispatcher.
 */
public interface ButtonCommandHandlerV2 {

    String getCommandName();

    default List<String> getAliases() {
        return List.of();
    }

    /**
     * Handle the command and return a domain result.
     * The result should not contain any JDA-specific objects.
     *
     * @param context JDA-free interaction context
     * @return A domain event representing the command outcome
     */
    AbstractCommandResultV2<?> handleCommand(ButtonInteractionContext context);
}
