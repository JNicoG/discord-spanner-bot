package io.github.jnicog.discord.spanner.bot.command.handler;

import io.github.jnicog.discord.spanner.bot.command.ButtonInteractionContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

import java.util.List;

/**
 * Handler interface that receives JDA-free context.
 * No responder is passed - responses are handled by the dispatcher.
 */
public interface ButtonCommandHandler {

    String getCommandName();

    default List<String> getAliases() {
        return List.of();
    }

    default boolean matchesComponentId(String componentId) {
        return getCommandName().equals(componentId);
    }

    /**
     * Handle the command and return a domain result.
     * The result should not contain any JDA-specific objects.
     *
     * @param context JDA-free interaction context
     * @return A domain event representing the command outcome
     */
    AbstractCommandResult<?> handleCommand(ButtonInteractionContext context);
}
