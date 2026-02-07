package io.github.jnicog.discord.spanner.bot.leaderboard;

import java.util.List;

/**
 * Represents a page of leaderboard entries.
 *
 * @param entries The entries on this page
 * @param currentPage The current page number (1-based)
 * @param totalPages The total number of pages
 * @param totalEntries The total number of entries across all pages
 */
public record LeaderboardPage(
        List<LeaderboardEntry> entries,
        int currentPage,
        int totalPages,
        int totalEntries
) {
    public boolean hasNextPage() {
        return currentPage < totalPages;
    }

    public boolean hasPreviousPage() {
        return currentPage > 1;
    }

    public boolean isFirstPage() {
        return currentPage == 1;
    }

    public boolean isLastPage() {
        return currentPage == totalPages;
    }
}

