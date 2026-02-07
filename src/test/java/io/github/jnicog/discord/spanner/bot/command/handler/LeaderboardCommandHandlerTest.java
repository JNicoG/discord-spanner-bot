package io.github.jnicog.discord.spanner.bot.command.handler;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import io.github.jnicog.discord.spanner.bot.event.leaderboard.LeaderboardQueryEvent;
import io.github.jnicog.discord.spanner.bot.leaderboard.LeaderboardEntry;
import io.github.jnicog.discord.spanner.bot.leaderboard.LeaderboardPage;
import io.github.jnicog.discord.spanner.bot.leaderboard.LeaderboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LeaderboardCommandHandler")
class LeaderboardCommandHandlerTest {

    private static final long USER_ID = 123456789L;
    private static final long CHANNEL_ID = 111111111L;

    @Mock
    private LeaderboardService leaderboardService;

    private LeaderboardCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new LeaderboardCommandHandler(leaderboardService);
    }

    private SlashCommandContext createContext() {
        return new SlashCommandContext(
                OffsetDateTime.now(),
                "leaderboard",
                USER_ID,
                CHANNEL_ID,
                Map.of()
        );
    }

    @Test
    @DisplayName("should return LeaderboardQueryEvent with first page")
    void shouldReturnLeaderboardQueryEventWithFirstPage() {
        // Given
        List<LeaderboardEntry> entries = List.of(
                new LeaderboardEntry(1, 111L, 10),
                new LeaderboardEntry(2, 222L, 5)
        );
        LeaderboardPage page = new LeaderboardPage(entries, 1, 1, 2);
        when(leaderboardService.getFirstPage(CHANNEL_ID)).thenReturn(page);

        // When
        AbstractCommandResult<?> result = handler.handleCommand(createContext());

        // Then
        assertInstanceOf(LeaderboardQueryEvent.class, result);
        LeaderboardQueryEvent event = (LeaderboardQueryEvent) result;
        assertEquals(page, event.getLeaderboardPage());
    }

    @Test
    @DisplayName("should call leaderboardService.getFirstPage with correct channel ID")
    void shouldCallGetFirstPageWithCorrectChannelId() {
        // Given
        LeaderboardPage page = new LeaderboardPage(List.of(), 1, 1, 0);
        when(leaderboardService.getFirstPage(CHANNEL_ID)).thenReturn(page);

        // When
        handler.handleCommand(createContext());

        // Then
        verify(leaderboardService).getFirstPage(CHANNEL_ID);
    }

    @Test
    @DisplayName("should return correct command name")
    void shouldReturnCorrectCommandName() {
        assertEquals("leaderboard", handler.getCommandName());
    }
}

