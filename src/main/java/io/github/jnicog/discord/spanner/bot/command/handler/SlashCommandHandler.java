package io.github.jnicog.discord.spanner.bot.command.handler;

import io.github.jnicog.discord.spanner.bot.command.CommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

import java.util.List;

public interface SlashCommandHandler {
    String getCommandName();
    default List<String> getAliases() {
        return List.of();
    }

    // Unsure if this should be kept in.
    // This is for commands that are already determined to be ephemeral or public
    // so that the dispatcher has the ability to defer a response properly if needed
    boolean isEphemeral();

    AbstractCommandResult<?> handleCommand(CommandContext context);
}
