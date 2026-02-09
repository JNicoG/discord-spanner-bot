package io.github.jnicog.discord.spanner.bot.event.leaderboard;

import io.github.jnicog.discord.spanner.bot.leaderboard.LeaderboardPage;

/**
 * Event published when a leaderboard page update needs to be sent.
 * Contains the channel, message, and new page to display.
 */
public record LeaderboardPageUpdateEvent(
        long channelId,
        long messageId,
        LeaderboardPage page
) {}

