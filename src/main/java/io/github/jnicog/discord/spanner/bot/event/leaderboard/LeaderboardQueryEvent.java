package io.github.jnicog.discord.spanner.bot.event.leaderboard;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import io.github.jnicog.discord.spanner.bot.leaderboard.LeaderboardPage;

/**
 * Event representing a leaderboard query result.
 */
public class LeaderboardQueryEvent extends AbstractCommandResult<SlashCommandContext> {

    private final LeaderboardPage leaderboardPage;

    public LeaderboardQueryEvent(SlashCommandContext context, LeaderboardPage leaderboardPage) {
        super(context);
        this.leaderboardPage = leaderboardPage;
    }

    public LeaderboardPage getLeaderboardPage() {
        return leaderboardPage;
    }
}

