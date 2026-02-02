package io.github.jnicog.discord.spanner.bot.command.handler;

import io.github.jnicog.discord.spanner.bot.command.CommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

import java.util.List;

public interface SlashCommandHandler {
    String getCommandName();
    default List<String> getAliases() {
        return List.of();
    }
    boolean isEphemeral();
    AbstractCommandResult handleCommand(CommandContext context);
}
