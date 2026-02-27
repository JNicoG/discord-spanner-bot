package io.github.jnicog.discord.spanner.bot.command.registry;

import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpannerBotSlashCommandRegistry implements SlashCommandRegistry {
    @Override
    public List<CommandData> getCommands() {
        return List.of(
                Commands.slash("k", "Join the queue")
                        .setContexts(InteractionContextType.GUILD),
                Commands.slash("keen", "Join the queue")
                        .setContexts(InteractionContextType.GUILD),
                Commands.slash("unkeen", "Leave the queue")
                        .setContexts(InteractionContextType.GUILD),
                Commands.slash("keeners", "Show current queue members")
                    .setContexts(InteractionContextType.GUILD),
                Commands.slash("spanners", "Display the number of spanners a user has accumulated")
                        .setContexts(InteractionContextType.GUILD)
                        .addOption(OptionType.USER, "user", "The user to perform a lookup against.", false),
                Commands.slash("leaderboard", "Display the leaderboard for this message channel")
                        .setContexts(InteractionContextType.GUILD),
                Commands.slash("ten-man", "Create a 10-man availability poll")
                        .setContexts(InteractionContextType.GUILD)
                        .addOptions(
                                new OptionData(OptionType.STRING, "start_date", "Start date (yyyy-MM-dd)", true),
                                new OptionData(OptionType.STRING, "end_date", "End date (yyyy-MM-dd)", true),
                                new OptionData(OptionType.STRING, "time", "Preferred time (display only)", false),
                                new OptionData(OptionType.BOOLEAN, "test", "Test mode — 1 sign-up triggers match created", false),
                                new OptionData(OptionType.STRING, "duration", "How long the poll stays open (default: 24 hours)", false)
                                        .addChoices(
                                                new net.dv8tion.jda.api.interactions.commands.Command.Choice("1 hour", "1"),
                                                new net.dv8tion.jda.api.interactions.commands.Command.Choice("2 hours", "2"),
                                                new net.dv8tion.jda.api.interactions.commands.Command.Choice("6 hours", "6"),
                                                new net.dv8tion.jda.api.interactions.commands.Command.Choice("24 hours (default)", "24"),
                                                new net.dv8tion.jda.api.interactions.commands.Command.Choice("48 hours", "48"),
                                                new net.dv8tion.jda.api.interactions.commands.Command.Choice("72 hours", "72")
                                        )
                        ),
                Commands.slash("ten-man-cancel", "Cancel the active 10-man poll in this channel")
                        .setContexts(InteractionContextType.GUILD)
        );
    }
}
