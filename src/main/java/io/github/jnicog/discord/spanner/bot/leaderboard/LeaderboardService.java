package io.github.jnicog.discord.spanner.bot.leaderboard;

import io.github.jnicog.discord.spanner.bot.repository.SpannerRepository;
import io.github.jnicog.discord.spanner.bot.repository.entity.SpannerEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Service for leaderboard operations.
 */
@Service
@Transactional(readOnly = true)
public class LeaderboardService {

    private static final int PAGE_SIZE = 5;

    private final SpannerRepository spannerRepository;

    public LeaderboardService(SpannerRepository spannerRepository) {
        this.spannerRepository = spannerRepository;
    }

    /**
     * Gets a page of the leaderboard for a specific channel.
     *
     * @param channelId The channel ID
     * @param page The page number (1-based)
     * @return The leaderboard page
     */
    public LeaderboardPage getLeaderboardPage(long channelId, int page) {
        List<SpannerEntity> allEntries = spannerRepository.findByChannelId(channelId);

        // Sort by spanner count descending
        List<SpannerEntity> sorted = allEntries.stream()
                .sorted(Comparator.comparingInt(SpannerEntity::getSpannerCount).reversed())
                .toList();

        int totalEntries = sorted.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalEntries / PAGE_SIZE));

        // Clamp page to valid range
        int validPage = Math.max(1, Math.min(page, totalPages));

        int startIndex = (validPage - 1) * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, totalEntries);

        List<LeaderboardEntry> entries = IntStream.range(startIndex, endIndex)
                .mapToObj(i -> {
                    SpannerEntity entity = sorted.get(i);
                    return new LeaderboardEntry(i + 1, entity.getUserId(), entity.getSpannerCount());
                })
                .toList();

        return new LeaderboardPage(entries, validPage, totalPages, totalEntries);
    }

    /**
     * Gets the first page of the leaderboard.
     */
    public LeaderboardPage getFirstPage(long channelId) {
        return getLeaderboardPage(channelId, 1);
    }

    /**
     * Gets the last page of the leaderboard.
     */
    public LeaderboardPage getLastPage(long channelId) {
        List<SpannerEntity> allEntries = spannerRepository.findByChannelId(channelId);
        int totalPages = Math.max(1, (int) Math.ceil((double) allEntries.size() / PAGE_SIZE));
        return getLeaderboardPage(channelId, totalPages);
    }
}

