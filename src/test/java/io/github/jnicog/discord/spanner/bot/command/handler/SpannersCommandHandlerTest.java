package io.github.jnicog.discord.spanner.bot.command.handler;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import io.github.jnicog.discord.spanner.bot.event.spanner.SpannerQueryEvent;
import io.github.jnicog.discord.spanner.bot.spanner.SpannerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SpannersCommandHandler")
class SpannersCommandHandlerTest {

    private static final long USER_ID = 123456789L;
    private static final long TARGET_USER_ID = 987654321L;
    private static final long CHANNEL_ID = 111111111L;

    @Mock
    private SpannerService spannerService;

    private SpannersCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new SpannersCommandHandler(spannerService);
    }

    private SlashCommandContext createContext(Map<String, String> options) {
        return new SlashCommandContext(
                OffsetDateTime.now(),
                "spanners",
                USER_ID,
                CHANNEL_ID,
                options
        );
    }

    @Nested
    @DisplayName("When querying own spanner count (no user option)")
    class SelfQueryTests {

        @Test
        @DisplayName("should return SpannerQueryEvent with own spanner count")
        void shouldReturnSpannerQueryEventWithOwnCount() {
            // Given
            SlashCommandContext context = createContext(Map.of());
            when(spannerService.getOrCreateSpannerCount(USER_ID, CHANNEL_ID)).thenReturn(5);

            // When
            AbstractCommandResult<?> result = handler.handleCommand(context);

            // Then
            assertInstanceOf(SpannerQueryEvent.class, result);
            SpannerQueryEvent event = (SpannerQueryEvent) result;
            assertEquals(USER_ID, event.getTargetUserId());
            assertEquals(5, event.getSpannerCount());
            assertTrue(event.isSelfQuery());
        }

        @Test
        @DisplayName("should create record if user has no existing spanner entry")
        void shouldCreateRecordIfNoExistingEntry() {
            // Given
            SlashCommandContext context = createContext(Map.of());
            when(spannerService.getOrCreateSpannerCount(USER_ID, CHANNEL_ID)).thenReturn(0);

            // When
            AbstractCommandResult<?> result = handler.handleCommand(context);

            // Then
            verify(spannerService).getOrCreateSpannerCount(USER_ID, CHANNEL_ID);
            SpannerQueryEvent event = (SpannerQueryEvent) result;
            assertEquals(0, event.getSpannerCount());
        }

        @Test
        @DisplayName("should return self query when user option is empty string")
        void shouldReturnSelfQueryWhenUserOptionIsEmpty() {
            // Given
            SlashCommandContext context = createContext(Map.of("user", ""));
            when(spannerService.getOrCreateSpannerCount(USER_ID, CHANNEL_ID)).thenReturn(3);

            // When
            AbstractCommandResult<?> result = handler.handleCommand(context);

            // Then
            SpannerQueryEvent event = (SpannerQueryEvent) result;
            assertTrue(event.isSelfQuery());
            assertEquals(USER_ID, event.getTargetUserId());
        }

        @Test
        @DisplayName("should return self query when user option is blank")
        void shouldReturnSelfQueryWhenUserOptionIsBlank() {
            // Given
            SlashCommandContext context = createContext(Map.of("user", "   "));
            when(spannerService.getOrCreateSpannerCount(USER_ID, CHANNEL_ID)).thenReturn(3);

            // When
            AbstractCommandResult<?> result = handler.handleCommand(context);

            // Then
            SpannerQueryEvent event = (SpannerQueryEvent) result;
            assertTrue(event.isSelfQuery());
        }
    }

    @Nested
    @DisplayName("When querying another user's spanner count")
    class UserQueryTests {

        @Test
        @DisplayName("should return SpannerQueryEvent with target user's spanner count")
        void shouldReturnSpannerQueryEventWithTargetUserCount() {
            // Given
            SlashCommandContext context = createContext(Map.of("user", String.valueOf(TARGET_USER_ID)));
            when(spannerService.getOrCreateSpannerCount(USER_ID, CHANNEL_ID)).thenReturn(2);
            when(spannerService.getOrCreateSpannerCount(TARGET_USER_ID, CHANNEL_ID)).thenReturn(10);

            // When
            AbstractCommandResult<?> result = handler.handleCommand(context);

            // Then
            assertInstanceOf(SpannerQueryEvent.class, result);
            SpannerQueryEvent event = (SpannerQueryEvent) result;
            assertEquals(TARGET_USER_ID, event.getTargetUserId());
            assertEquals(10, event.getSpannerCount());
            assertFalse(event.isSelfQuery());
        }

        @Test
        @DisplayName("should create records for both requesting and target user")
        void shouldCreateRecordsForBothUsers() {
            // Given
            SlashCommandContext context = createContext(Map.of("user", String.valueOf(TARGET_USER_ID)));
            when(spannerService.getOrCreateSpannerCount(USER_ID, CHANNEL_ID)).thenReturn(0);
            when(spannerService.getOrCreateSpannerCount(TARGET_USER_ID, CHANNEL_ID)).thenReturn(0);

            // When
            handler.handleCommand(context);

            // Then
            verify(spannerService).getOrCreateSpannerCount(USER_ID, CHANNEL_ID);
            verify(spannerService).getOrCreateSpannerCount(TARGET_USER_ID, CHANNEL_ID);
        }

        @Test
        @DisplayName("should return isSelfQuery true when querying own ID explicitly")
        void shouldReturnSelfQueryTrueWhenQueryingOwnId() {
            // Given
            SlashCommandContext context = createContext(Map.of("user", String.valueOf(USER_ID)));
            when(spannerService.getOrCreateSpannerCount(USER_ID, CHANNEL_ID)).thenReturn(7);

            // When
            AbstractCommandResult<?> result = handler.handleCommand(context);

            // Then
            SpannerQueryEvent event = (SpannerQueryEvent) result;
            assertTrue(event.isSelfQuery());
            assertEquals(USER_ID, event.getTargetUserId());
        }

        @Test
        @DisplayName("should fallback to self query when user ID is invalid")
        void shouldFallbackToSelfQueryWhenUserIdIsInvalid() {
            // Given
            SlashCommandContext context = createContext(Map.of("user", "not-a-number"));
            when(spannerService.getOrCreateSpannerCount(USER_ID, CHANNEL_ID)).thenReturn(4);

            // When
            AbstractCommandResult<?> result = handler.handleCommand(context);

            // Then
            SpannerQueryEvent event = (SpannerQueryEvent) result;
            assertTrue(event.isSelfQuery());
            assertEquals(USER_ID, event.getTargetUserId());
            assertEquals(4, event.getSpannerCount());
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle zero spanner count correctly")
        void shouldHandleZeroSpannerCount() {
            // Given
            SlashCommandContext context = createContext(Map.of());
            when(spannerService.getOrCreateSpannerCount(USER_ID, CHANNEL_ID)).thenReturn(0);

            // When
            AbstractCommandResult<?> result = handler.handleCommand(context);

            // Then
            SpannerQueryEvent event = (SpannerQueryEvent) result;
            assertEquals(0, event.getSpannerCount());
        }

        @Test
        @DisplayName("should handle high spanner count correctly")
        void shouldHandleHighSpannerCount() {
            // Given
            SlashCommandContext context = createContext(Map.of());
            when(spannerService.getOrCreateSpannerCount(USER_ID, CHANNEL_ID)).thenReturn(999);

            // When
            AbstractCommandResult<?> result = handler.handleCommand(context);

            // Then
            SpannerQueryEvent event = (SpannerQueryEvent) result;
            assertEquals(999, event.getSpannerCount());
        }
    }
}

