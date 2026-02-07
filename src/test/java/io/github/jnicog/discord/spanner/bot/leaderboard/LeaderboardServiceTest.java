package io.github.jnicog.discord.spanner.bot.leaderboard;

import io.github.jnicog.discord.spanner.bot.repository.SpannerRepository;
import io.github.jnicog.discord.spanner.bot.repository.entity.SpannerEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LeaderboardService")
class LeaderboardServiceTest {

    private static final long CHANNEL_ID = 111111111L;

    @Mock
    private SpannerRepository spannerRepository;

    private LeaderboardService leaderboardService;

    @BeforeEach
    void setUp() {
        leaderboardService = new LeaderboardService(spannerRepository);
    }

    private SpannerEntity createSpannerEntity(long userId, int spannerCount) {
        SpannerEntity entity = new SpannerEntity(userId, CHANNEL_ID);
        for (int i = 0; i < spannerCount; i++) {
            entity.incrementSpannerCount();
        }
        return entity;
    }

    @Nested
    @DisplayName("When getting leaderboard page")
    class GetLeaderboardPageTests {

        @Test
        @DisplayName("should return empty page when no entries exist")
        void shouldReturnEmptyPageWhenNoEntries() {
            // Given
            when(spannerRepository.findByChannelId(CHANNEL_ID)).thenReturn(List.of());

            // When
            LeaderboardPage page = leaderboardService.getLeaderboardPage(CHANNEL_ID, 1);

            // Then
            assertEquals(1, page.currentPage());
            assertEquals(1, page.totalPages());
            assertEquals(0, page.totalEntries());
            assertTrue(page.entries().isEmpty());
        }

        @Test
        @DisplayName("should return entries sorted by spanner count descending")
        void shouldReturnEntriesSortedBySpannerCountDescending() {
            // Given
            List<SpannerEntity> entities = List.of(
                    createSpannerEntity(1L, 5),
                    createSpannerEntity(2L, 10),
                    createSpannerEntity(3L, 3)
            );
            when(spannerRepository.findByChannelId(CHANNEL_ID)).thenReturn(entities);

            // When
            LeaderboardPage page = leaderboardService.getLeaderboardPage(CHANNEL_ID, 1);

            // Then
            assertEquals(3, page.entries().size());
            assertEquals(2L, page.entries().get(0).userId()); // 10 spanners
            assertEquals(1L, page.entries().get(1).userId()); // 5 spanners
            assertEquals(3L, page.entries().get(2).userId()); // 3 spanners
        }

        @Test
        @DisplayName("should assign correct ranks")
        void shouldAssignCorrectRanks() {
            // Given
            List<SpannerEntity> entities = List.of(
                    createSpannerEntity(1L, 10),
                    createSpannerEntity(2L, 5),
                    createSpannerEntity(3L, 3)
            );
            when(spannerRepository.findByChannelId(CHANNEL_ID)).thenReturn(entities);

            // When
            LeaderboardPage page = leaderboardService.getLeaderboardPage(CHANNEL_ID, 1);

            // Then
            assertEquals(1, page.entries().get(0).rank());
            assertEquals(2, page.entries().get(1).rank());
            assertEquals(3, page.entries().get(2).rank());
        }

        @Test
        @DisplayName("should paginate with 5 entries per page")
        void shouldPaginateWithFiveEntriesPerPage() {
            // Given - 12 entries, should be 3 pages (5, 5, 2)
            List<SpannerEntity> entities = new ArrayList<>();
            for (int i = 1; i <= 12; i++) {
                entities.add(createSpannerEntity(i, 100 - i));
            }
            when(spannerRepository.findByChannelId(CHANNEL_ID)).thenReturn(entities);

            // When - get first page
            LeaderboardPage page1 = leaderboardService.getLeaderboardPage(CHANNEL_ID, 1);

            // Then
            assertEquals(1, page1.currentPage());
            assertEquals(3, page1.totalPages());
            assertEquals(12, page1.totalEntries());
            assertEquals(5, page1.entries().size());
            assertEquals(1, page1.entries().get(0).rank());
            assertEquals(5, page1.entries().get(4).rank());
        }

        @Test
        @DisplayName("should return correct entries for second page")
        void shouldReturnCorrectEntriesForSecondPage() {
            // Given - 12 entries
            List<SpannerEntity> entities = new ArrayList<>();
            for (int i = 1; i <= 12; i++) {
                entities.add(createSpannerEntity(i, 100 - i));
            }
            when(spannerRepository.findByChannelId(CHANNEL_ID)).thenReturn(entities);

            // When - get second page
            LeaderboardPage page2 = leaderboardService.getLeaderboardPage(CHANNEL_ID, 2);

            // Then
            assertEquals(2, page2.currentPage());
            assertEquals(5, page2.entries().size());
            assertEquals(6, page2.entries().get(0).rank());
            assertEquals(10, page2.entries().get(4).rank());
        }

        @Test
        @DisplayName("should return last page with remaining entries")
        void shouldReturnLastPageWithRemainingEntries() {
            // Given - 12 entries, last page should have 2
            List<SpannerEntity> entities = new ArrayList<>();
            for (int i = 1; i <= 12; i++) {
                entities.add(createSpannerEntity(i, 100 - i));
            }
            when(spannerRepository.findByChannelId(CHANNEL_ID)).thenReturn(entities);

            // When - get last page
            LeaderboardPage page3 = leaderboardService.getLeaderboardPage(CHANNEL_ID, 3);

            // Then
            assertEquals(3, page3.currentPage());
            assertEquals(2, page3.entries().size());
            assertEquals(11, page3.entries().get(0).rank());
            assertEquals(12, page3.entries().get(1).rank());
        }

        @Test
        @DisplayName("should clamp page to valid range when too high")
        void shouldClampPageWhenTooHigh() {
            // Given
            List<SpannerEntity> entities = List.of(
                    createSpannerEntity(1L, 10)
            );
            when(spannerRepository.findByChannelId(CHANNEL_ID)).thenReturn(entities);

            // When - request page 100
            LeaderboardPage page = leaderboardService.getLeaderboardPage(CHANNEL_ID, 100);

            // Then - should return page 1 (the only page)
            assertEquals(1, page.currentPage());
        }

        @Test
        @DisplayName("should clamp page to 1 when zero or negative")
        void shouldClampPageWhenZeroOrNegative() {
            // Given
            List<SpannerEntity> entities = List.of(
                    createSpannerEntity(1L, 10)
            );
            when(spannerRepository.findByChannelId(CHANNEL_ID)).thenReturn(entities);

            // When - request page 0
            LeaderboardPage page = leaderboardService.getLeaderboardPage(CHANNEL_ID, 0);

            // Then - should return page 1
            assertEquals(1, page.currentPage());
        }
    }

    @Nested
    @DisplayName("LeaderboardPage navigation helpers")
    class PageNavigationTests {

        @Test
        @DisplayName("hasNextPage should return true when not on last page")
        void hasNextPageShouldReturnTrueWhenNotOnLastPage() {
            LeaderboardPage page = new LeaderboardPage(List.of(), 1, 3, 15);
            assertTrue(page.hasNextPage());
        }

        @Test
        @DisplayName("hasNextPage should return false when on last page")
        void hasNextPageShouldReturnFalseWhenOnLastPage() {
            LeaderboardPage page = new LeaderboardPage(List.of(), 3, 3, 15);
            assertFalse(page.hasNextPage());
        }

        @Test
        @DisplayName("hasPreviousPage should return true when not on first page")
        void hasPreviousPageShouldReturnTrueWhenNotOnFirstPage() {
            LeaderboardPage page = new LeaderboardPage(List.of(), 2, 3, 15);
            assertTrue(page.hasPreviousPage());
        }

        @Test
        @DisplayName("hasPreviousPage should return false when on first page")
        void hasPreviousPageShouldReturnFalseWhenOnFirstPage() {
            LeaderboardPage page = new LeaderboardPage(List.of(), 1, 3, 15);
            assertFalse(page.hasPreviousPage());
        }
    }
}

