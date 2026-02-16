package io.github.jnicog.discord.spanner.bot.command.handler;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import io.github.jnicog.discord.spanner.bot.event.leaderboard.LeaderboardQueryEvent;
import io.github.jnicog.discord.spanner.bot.leaderboard.LeaderboardPage;
import io.github.jnicog.discord.spanner.bot.leaderboard.LeaderboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handler for the /leaderboard command.
 * Displays a paginated leaderboard of spanner counts for the channel.
 */
@Component
public class LeaderboardCommandHandler implements SlashCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeaderboardCommandHandler.class);

    private final LeaderboardService leaderboardService;

    public LeaderboardCommandHandler(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @Override
    public String getCommandName() {
        return "leaderboard";
    }

    @Override
    public AbstractCommandResult<?> handleCommand(SlashCommandContext context) {
        long channelId = context.channelId();

        LOGGER.debug("User {} requesting leaderboard for channel {}", context.userId(), channelId);

        LeaderboardPage page = leaderboardService.getFirstPage(channelId);

        return new LeaderboardQueryEvent(context, page);
    }
}

