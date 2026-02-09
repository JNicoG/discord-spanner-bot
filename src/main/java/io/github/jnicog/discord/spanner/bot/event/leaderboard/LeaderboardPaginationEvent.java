package io.github.jnicog.discord.spanner.bot.event.leaderboard;

import io.github.jnicog.discord.spanner.bot.leaderboard.LeaderboardPaginationAction;
import io.github.jnicog.discord.spanner.bot.leaderboard.LeaderboardSession;

/**
 * Event published when a valid leaderboard pagination reaction is received.
 * Contains the session and action for the handler to process.
 */
public record LeaderboardPaginationEvent(
        long channelId,
        long messageId,
        LeaderboardSession session,
        LeaderboardPaginationAction action
) {}

