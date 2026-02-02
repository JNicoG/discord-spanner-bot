package io.github.jnicog.discord.spanner.bot.command.registry;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.List;

public interface SlashCommandRegistry {
    List<CommandData> getCommands();
}
