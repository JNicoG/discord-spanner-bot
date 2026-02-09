package io.github.jnicog.discord.spanner.bot.event.leaderboard;

import io.github.jnicog.discord.spanner.bot.leaderboard.LeaderboardSession;

/**
 * Event published when a leaderboard session times out.
 * The handler should clear reactions from the message.
 */
public record LeaderboardSessionTimeoutEvent(LeaderboardSession session) {}

