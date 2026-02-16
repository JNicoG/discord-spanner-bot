package io.github.jnicog.discord.spanner.bot.leaderboard;

/**
 * Represents a single entry in the leaderboard.
 *
 * @param rank The rank/position (1-based)
 * @param userId The Discord user ID
 * @param spannerCount The number of spanners
 */
public record LeaderboardEntry(
        int rank,
        long userId,
        int spannerCount
) {}

