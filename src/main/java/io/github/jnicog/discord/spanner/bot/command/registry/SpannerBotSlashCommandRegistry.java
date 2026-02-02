package io.github.jnicog.discord.spanner.bot.command.registry;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpannerBotSlashCommandRegistry implements SlashCommandRegistry {
    @Override
    public List<CommandData> getCommands() {
        return List.of(
                Commands.slash("k", "Join the queue"),
                Commands.slash("keen", "Join the queue"),
                Commands.slash("unkeen", "Leave the queue"),
                Commands.slash("keeners", "Show current queue members"),
                Commands.slash("spanners", "Display the number of spanners a user has accumulated")
                        .addOption(OptionType.USER, "user", "The user to perform a lookup against.", false),
                Commands.slash("leaderboard", "Display the leaderboard for this message channel")
        );
    }
}
