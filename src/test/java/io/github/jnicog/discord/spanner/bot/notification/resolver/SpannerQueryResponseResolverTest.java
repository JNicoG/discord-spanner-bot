package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.spanner.SpannerQueryEvent;
import io.github.jnicog.discord.spanner.bot.notification.MessageFormatterService;
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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SpannerQueryResponseResolver")
class SpannerQueryResponseResolverTest {

    private static final long USER_ID = 123456789L;
    private static final long CHANNEL_ID = 111111111L;

    @Mock
    private MessageFormatterService messageFormatter;

    private SpannerQueryResponseResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new SpannerQueryResponseResolver(messageFormatter);
    }

    private SlashCommandContext createContext() {
        return new SlashCommandContext(
                OffsetDateTime.now(),
                "spanners",
                USER_ID,
                CHANNEL_ID,
                Map.of()
        );
    }

    @Nested
    @DisplayName("When resolving self query")
    class SelfQueryTests {

        @Test
        @DisplayName("should format message for self query")
        void shouldFormatMessageForSelfQuery() {
            // Given
            SpannerQueryEvent event = new SpannerQueryEvent(createContext(), USER_ID, 5, true);
            when(messageFormatter.formatSelfSpannerCount(5)).thenReturn("You have spannered **5** times");

            // When
            InteractionResponse response = resolver.resolve(event);

            // Then
            verify(messageFormatter).formatSelfSpannerCount(5);
            verify(messageFormatter, never()).formatUserSpannerCount(anyLong(), anyInt());
            assertInstanceOf(InteractionResponse.PublicReply.class, response);
        }

        @Test
        @DisplayName("should return public reply for self query")
        void shouldReturnPublicReplyForSelfQuery() {
            // Given
            SpannerQueryEvent event = new SpannerQueryEvent(createContext(), USER_ID, 0, true);
            when(messageFormatter.formatSelfSpannerCount(0)).thenReturn("You have spannered **0** times");

            // When
            InteractionResponse response = resolver.resolve(event);

            // Then
            assertInstanceOf(InteractionResponse.PublicReply.class, response);
            InteractionResponse.PublicReply publicReply = (InteractionResponse.PublicReply) response;
            assertEquals("You have spannered **0** times", publicReply.content());
        }
    }

    @Nested
    @DisplayName("When resolving user query")
    class UserQueryTests {

        @Test
        @DisplayName("should format message for user query")
        void shouldFormatMessageForUserQuery() {
            // Given
            long targetUserId = 987654321L;
            SpannerQueryEvent event = new SpannerQueryEvent(createContext(), targetUserId, 10, false);
            when(messageFormatter.formatUserSpannerCount(targetUserId, 10))
                    .thenReturn("<@987654321> has spannered **10** times");

            // When
            InteractionResponse response = resolver.resolve(event);

            // Then
            verify(messageFormatter).formatUserSpannerCount(targetUserId, 10);
            verify(messageFormatter, never()).formatSelfSpannerCount(anyInt());
            assertInstanceOf(InteractionResponse.PublicReply.class, response);
        }

        @Test
        @DisplayName("should return public reply for user query")
        void shouldReturnPublicReplyForUserQuery() {
            // Given
            long targetUserId = 987654321L;
            SpannerQueryEvent event = new SpannerQueryEvent(createContext(), targetUserId, 3, false);
            when(messageFormatter.formatUserSpannerCount(targetUserId, 3))
                    .thenReturn("<@987654321> has spannered **3** times");

            // When
            InteractionResponse response = resolver.resolve(event);

            // Then
            assertInstanceOf(InteractionResponse.PublicReply.class, response);
            InteractionResponse.PublicReply publicReply = (InteractionResponse.PublicReply) response;
            assertEquals("<@987654321> has spannered **3** times", publicReply.content());
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle singular spanner correctly")
        void shouldHandleSingularSpannerCorrectly() {
            // Given
            SpannerQueryEvent event = new SpannerQueryEvent(createContext(), USER_ID, 1, true);
            when(messageFormatter.formatSelfSpannerCount(1)).thenReturn("You have spannered **1** time");

            // When
            InteractionResponse response = resolver.resolve(event);

            // Then
            InteractionResponse.PublicReply publicReply = (InteractionResponse.PublicReply) response;
            assertEquals("You have spannered **1** time", publicReply.content());
        }
    }
}



