package io.github.jnicog.discord.spanner.bot.command.handler;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManInvalidDateRangeEvent;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManNotAuthorisedEvent;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManPollAlreadyActiveEvent;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManPollCreatedEvent;
import io.github.jnicog.discord.spanner.bot.tenman.TenManDateOptionSnapshot;
import io.github.jnicog.discord.spanner.bot.tenman.TenManPermissionChecker;
import io.github.jnicog.discord.spanner.bot.tenman.TenManPollCreatedResult;
import io.github.jnicog.discord.spanner.bot.tenman.TenManService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TenManCommandHandler")
class TenManCommandHandlerTest {

    private static final long USER_ID = 123456789L;
    private static final long CHANNEL_ID = 111111111L;

    @Mock
    private TenManService tenManService;

    @Mock
    private TenManPermissionChecker permissionChecker;

    private TenManCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new TenManCommandHandler(tenManService, permissionChecker);
    }

    private SlashCommandContext createContext(Map<String, String> options) {
        return new SlashCommandContext(
                OffsetDateTime.now(),
                "ten-man",
                USER_ID,
                "testuser",
                CHANNEL_ID,
                options
        );
    }

    private Map<String, String> validOptions() {
        return Map.of(
                "start_date", "2026-03-10",
                "end_date", "2026-03-12",
                "time", "20:00"
        );
    }

    @Test
    @DisplayName("should return TenManNotAuthorisedEvent when user not permitted")
    void shouldReturnNotAuthorisedEvent_whenUserNotPermitted() {
        // Given
        when(permissionChecker.isAllowed("testuser")).thenReturn(false);

        // When
        AbstractCommandResult<?> result = handler.handleCommand(createContext(validOptions()));

        // Then
        assertInstanceOf(TenManNotAuthorisedEvent.class, result);
    }

    @Test
    @DisplayName("should return TenManInvalidDateRangeEvent when date format is bad")
    void shouldReturnInvalidDateRange_whenDateFormatBad() {
        // Given
        when(permissionChecker.isAllowed("testuser")).thenReturn(true);
        Map<String, String> options = Map.of(
                "start_date", "not-a-date",
                "end_date", "2026-03-12",
                "time", "20:00"
        );

        // When
        AbstractCommandResult<?> result = handler.handleCommand(createContext(options));

        // Then
        assertInstanceOf(TenManInvalidDateRangeEvent.class, result);
    }

    @Test
    @DisplayName("should return TenManInvalidDateRangeEvent when start date is after end date")
    void shouldReturnInvalidDateRange_whenStartAfterEnd() {
        // Given
        when(permissionChecker.isAllowed("testuser")).thenReturn(true);
        Map<String, String> options = Map.of(
                "start_date", "2026-03-15",
                "end_date", "2026-03-10",
                "time", "20:00"
        );

        // When
        AbstractCommandResult<?> result = handler.handleCommand(createContext(options));

        // Then
        assertInstanceOf(TenManInvalidDateRangeEvent.class, result);
        assertEquals("Start date must be on or before end date.",
                ((TenManInvalidDateRangeEvent) result).getErrorMessage());
    }

    @Test
    @DisplayName("should return TenManInvalidDateRangeEvent when range exceeds 14 days")
    void shouldReturnInvalidDateRange_whenRangeExceeds14Days() {
        // Given
        when(permissionChecker.isAllowed("testuser")).thenReturn(true);
        Map<String, String> options = Map.of(
                "start_date", "2026-03-01",
                "end_date", "2026-03-16",  // 15 days apart
                "time", "20:00"
        );

        // When
        AbstractCommandResult<?> result = handler.handleCommand(createContext(options));

        // Then
        assertInstanceOf(TenManInvalidDateRangeEvent.class, result);
        assertEquals("Date range must be 14 days or fewer.",
                ((TenManInvalidDateRangeEvent) result).getErrorMessage());
    }

    @Test
    @DisplayName("should return TenManPollAlreadyActiveEvent when service returns empty")
    void shouldReturnPollAlreadyActive_whenServiceReturnsEmpty() {
        // Given
        when(permissionChecker.isAllowed("testuser")).thenReturn(true);
        when(tenManService.createPoll(anyLong(), anyLong(), anyList(), anyString(), anyBoolean(), any()))
                .thenReturn(Optional.empty());

        // When
        AbstractCommandResult<?> result = handler.handleCommand(createContext(validOptions()));

        // Then
        assertInstanceOf(TenManPollAlreadyActiveEvent.class, result);
    }

    @Test
    @DisplayName("should return TenManPollCreatedEvent on valid input")
    void shouldReturnPollCreatedEvent_whenValidInput() {
        // Given
        when(permissionChecker.isAllowed("testuser")).thenReturn(true);
        TenManDateOptionSnapshot snapshot = new TenManDateOptionSnapshot(1L, LocalDate.of(2026, 3, 10), List.of());
        TenManPollCreatedResult created = new TenManPollCreatedResult(
                42L, "20:00", false, OffsetDateTime.now().plusHours(24), List.of(snapshot));
        when(tenManService.createPoll(anyLong(), anyLong(), anyList(), anyString(), anyBoolean(), any()))
                .thenReturn(Optional.of(created));

        // When
        AbstractCommandResult<?> result = handler.handleCommand(createContext(validOptions()));

        // Then
        assertInstanceOf(TenManPollCreatedEvent.class, result);
        TenManPollCreatedEvent event = (TenManPollCreatedEvent) result;
        assertEquals(42L, event.getPollId());
        assertEquals(CHANNEL_ID, event.getChannelId());
    }

    @Test
    @DisplayName("should return correct command name")
    void shouldReturnCorrectCommandName() {
        assertEquals("ten-man", handler.getCommandName());
    }
}
