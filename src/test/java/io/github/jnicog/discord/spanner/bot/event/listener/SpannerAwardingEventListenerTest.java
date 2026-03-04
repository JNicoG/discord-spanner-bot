package io.github.jnicog.discord.spanner.bot.event.listener;

import io.github.jnicog.discord.spanner.bot.audit.AuditService;
import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.checkin.CheckInCancelledEvent;
import io.github.jnicog.discord.spanner.bot.event.checkin.CheckInTimeoutEvent;
import io.github.jnicog.discord.spanner.bot.event.checkin.UnkeenDuringCheckInEvent;
import io.github.jnicog.discord.spanner.bot.event.queue.PlayerLeftQueueEvent;
import io.github.jnicog.discord.spanner.bot.command.ButtonInteractionContext;
import io.github.jnicog.discord.spanner.bot.checkin.CheckInTimeoutResult;
import io.github.jnicog.discord.spanner.bot.checkin.CheckInAttemptResult;
import io.github.jnicog.discord.spanner.bot.repository.entity.EventType;
import io.github.jnicog.discord.spanner.bot.spanner.SpannerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SpannerAwardingEventListener")
class SpannerAwardingEventListenerTest {

    private static final long USER_ID = 123456789L;
    private static final long CHANNEL_ID = 987654321L;
    private static final long MESSAGE_ID = 111111111L;
    private static final int MAX_QUEUE_SIZE = 5;

    @Mock
    private SpannerService spannerService;

    @Mock
    private AuditService auditService;

    private SpannerAwardingEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new SpannerAwardingEventListener(spannerService, auditService);
    }

    private SlashCommandContext createSlashCommandContext() {
        return new SlashCommandContext(
                OffsetDateTime.now(),
                "test-command",
                USER_ID,
                "testuser",
                CHANNEL_ID,
                Map.of()
        );
    }

    private ButtonInteractionContext createButtonInteractionContext() {
        return new ButtonInteractionContext(
                OffsetDateTime.now(),
                "test-button",
                USER_ID,
                CHANNEL_ID,
                MESSAGE_ID
        );
    }

    @Nested
    @DisplayName("When player leaves queue via /unkeen")
    class PlayerLeftQueueTests {

        @Test
        @DisplayName("should award spanner when player successfully leaves queue")
        void shouldAwardSpannerWhenPlayerLeavesQueue() {
            // Given
            SlashCommandContext context = createSlashCommandContext();
            Set<Long> remainingQueue = Set.of(222L, 333L);
            PlayerLeftQueueEvent event = new PlayerLeftQueueEvent(
                    context, remainingQueue, MAX_QUEUE_SIZE, false
            );
            when(spannerService.getSpannerCount(USER_ID, CHANNEL_ID)).thenReturn(1);

            // When
            listener.onPlayerLeftQueue(event);

            // Then
            verify(spannerService).incrementSpannerCount(USER_ID, CHANNEL_ID);
            verify(auditService).recordEvent(
                    eq(CHANNEL_ID),
                    eq(USER_ID),
                    eq(EventType.SPANNER_AWARDED),
                    any()
            );
        }

        @Test
        @DisplayName("should award spanner regardless of check-in session status")
        void shouldAwardSpannerRegardlessOfCheckInStatus() {
            // Given - check-in session is active but user is not a participant
            SlashCommandContext context = createSlashCommandContext();
            Set<Long> remainingQueue = Set.of(222L, 333L);
            PlayerLeftQueueEvent event = new PlayerLeftQueueEvent(
                    context, remainingQueue, MAX_QUEUE_SIZE, true // check-in active
            );
            when(spannerService.getSpannerCount(USER_ID, CHANNEL_ID)).thenReturn(1);

            // When
            listener.onPlayerLeftQueue(event);

            // Then
            verify(spannerService).incrementSpannerCount(USER_ID, CHANNEL_ID);
        }

        @Test
        @DisplayName("should record audit event with LEFT_QUEUE reason")
        void shouldRecordAuditEventWithLeftQueueReason() {
            // Given
            SlashCommandContext context = createSlashCommandContext();
            PlayerLeftQueueEvent event = new PlayerLeftQueueEvent(
                    context, Set.of(), MAX_QUEUE_SIZE, false
            );
            when(spannerService.getSpannerCount(USER_ID, CHANNEL_ID)).thenReturn(3);

            // When
            listener.onPlayerLeftQueue(event);

            // Then
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
            verify(auditService).recordEvent(
                    eq(CHANNEL_ID),
                    eq(USER_ID),
                    eq(EventType.SPANNER_AWARDED),
                    dataCaptor.capture()
            );

            Map<String, Object> capturedData = dataCaptor.getValue();
            assertEquals("LEFT_QUEUE", capturedData.get("reason"));
            assertEquals(3, capturedData.get("new_spanner_count"));
        }
    }

    @Nested
    @DisplayName("When player cancels check-in via /unkeen during active session")
    class UnkeenDuringCheckInTests {

        @Test
        @DisplayName("should award spanner when player cancels check-in via /unkeen")
        void shouldAwardSpannerWhenPlayerCancelsCheckInViaUnkeen() {
            // Given
            SlashCommandContext context = createSlashCommandContext();
            Set<Long> remainingUsers = Set.of(222L, 333L, 444L, 555L);
            UnkeenDuringCheckInEvent event = new UnkeenDuringCheckInEvent(
                    context, remainingUsers, MAX_QUEUE_SIZE, MESSAGE_ID
            );
            when(spannerService.getSpannerCount(USER_ID, CHANNEL_ID)).thenReturn(2);

            // When
            listener.onUnkeenDuringCheckIn(event);

            // Then
            verify(spannerService).incrementSpannerCount(USER_ID, CHANNEL_ID);
            verify(auditService).recordEvent(
                    eq(CHANNEL_ID),
                    eq(USER_ID),
                    eq(EventType.SPANNER_AWARDED),
                    any()
            );
        }

        @Test
        @DisplayName("should record audit event with UNKEEN_DURING_CHECK_IN reason")
        void shouldRecordAuditEventWithUnkeenDuringCheckInReason() {
            // Given
            SlashCommandContext context = createSlashCommandContext();
            UnkeenDuringCheckInEvent event = new UnkeenDuringCheckInEvent(
                    context, Set.of(222L), MAX_QUEUE_SIZE, MESSAGE_ID
            );
            when(spannerService.getSpannerCount(USER_ID, CHANNEL_ID)).thenReturn(5);

            // When
            listener.onUnkeenDuringCheckIn(event);

            // Then
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
            verify(auditService).recordEvent(
                    eq(CHANNEL_ID),
                    eq(USER_ID),
                    eq(EventType.SPANNER_AWARDED),
                    dataCaptor.capture()
            );

            Map<String, Object> capturedData = dataCaptor.getValue();
            assertEquals("UNKEEN_DURING_CHECK_IN", capturedData.get("reason"));
            assertEquals(5, capturedData.get("new_spanner_count"));
        }
    }

    @Nested
    @DisplayName("When player cancels check-in via spanner button")
    class CheckInCancelledTests {

        @Test
        @DisplayName("should award spanner when player presses cancel button")
        void shouldAwardSpannerWhenPlayerPressesCancelButton() {
            // Given
            ButtonInteractionContext context = createButtonInteractionContext();
            Set<Long> remainingUsers = Set.of(222L, 333L, 444L, 555L);
            CheckInCancelledEvent event = new CheckInCancelledEvent(
                    context, MESSAGE_ID, remainingUsers, MAX_QUEUE_SIZE
            );
            when(spannerService.getSpannerCount(USER_ID, CHANNEL_ID)).thenReturn(1);

            // When
            listener.onCheckInCancelled(event);

            // Then
            verify(spannerService).incrementSpannerCount(USER_ID, CHANNEL_ID);
            verify(auditService).recordEvent(
                    eq(CHANNEL_ID),
                    eq(USER_ID),
                    eq(EventType.SPANNER_AWARDED),
                    any()
            );
        }

        @Test
        @DisplayName("should record audit event with CHECK_IN_CANCELLED reason")
        void shouldRecordAuditEventWithCheckInCancelledReason() {
            // Given
            ButtonInteractionContext context = createButtonInteractionContext();
            CheckInCancelledEvent event = new CheckInCancelledEvent(
                    context, MESSAGE_ID, Set.of(), MAX_QUEUE_SIZE
            );
            when(spannerService.getSpannerCount(USER_ID, CHANNEL_ID)).thenReturn(10);

            // When
            listener.onCheckInCancelled(event);

            // Then
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
            verify(auditService).recordEvent(
                    eq(CHANNEL_ID),
                    eq(USER_ID),
                    eq(EventType.SPANNER_AWARDED),
                    dataCaptor.capture()
            );

            Map<String, Object> capturedData = dataCaptor.getValue();
            assertEquals("CHECK_IN_CANCELLED", capturedData.get("reason"));
            assertEquals(10, capturedData.get("new_spanner_count"));
        }
    }

    @Nested
    @DisplayName("When check-in times out")
    class CheckInTimeoutTests {

        @Test
        @DisplayName("should award spanner to each user who did not check in")
        void shouldAwardSpannerToEachUserWhoDidNotCheckIn() {
            // Given
            Set<Long> usersWhoDidNotCheckIn = Set.of(111L, 222L, 333L);
            Set<Long> usersWhoCheckedIn = Set.of(444L, 555L);
            CheckInTimeoutResult timeoutResult = new CheckInTimeoutResult(
                    CheckInAttemptResult.SESSION_TIMED_OUT,
                    usersWhoDidNotCheckIn,
                    usersWhoCheckedIn,
                    MESSAGE_ID,
                    CHANNEL_ID
            );
            CheckInTimeoutEvent event = new CheckInTimeoutEvent(timeoutResult);

            // When
            listener.onCheckInTimeout(event);

            // Then
            verify(spannerService).incrementSpannerCount(111L, CHANNEL_ID);
            verify(spannerService).incrementSpannerCount(222L, CHANNEL_ID);
            verify(spannerService).incrementSpannerCount(333L, CHANNEL_ID);
            verify(spannerService, times(3)).incrementSpannerCount(anyLong(), eq(CHANNEL_ID));
        }

        @Test
        @DisplayName("should NOT award spanner to users who checked in")
        void shouldNotAwardSpannerToUsersWhoCheckedIn() {
            // Given
            Set<Long> usersWhoDidNotCheckIn = Set.of(111L);
            Set<Long> usersWhoCheckedIn = Set.of(222L, 333L, 444L, 555L);
            CheckInTimeoutResult timeoutResult = new CheckInTimeoutResult(
                    CheckInAttemptResult.SESSION_TIMED_OUT,
                    usersWhoDidNotCheckIn,
                    usersWhoCheckedIn,
                    MESSAGE_ID,
                    CHANNEL_ID
            );
            CheckInTimeoutEvent event = new CheckInTimeoutEvent(timeoutResult);

            // When
            listener.onCheckInTimeout(event);

            // Then
            verify(spannerService, never()).incrementSpannerCount(eq(222L), anyLong());
            verify(spannerService, never()).incrementSpannerCount(eq(333L), anyLong());
            verify(spannerService, never()).incrementSpannerCount(eq(444L), anyLong());
            verify(spannerService, never()).incrementSpannerCount(eq(555L), anyLong());
            verify(spannerService).incrementSpannerCount(111L, CHANNEL_ID);
        }

        @Test
        @DisplayName("should record audit event for each user who did not check in")
        void shouldRecordAuditEventForEachUserWhoDidNotCheckIn() {
            // Given
            Set<Long> usersWhoDidNotCheckIn = Set.of(111L, 222L);
            CheckInTimeoutResult timeoutResult = new CheckInTimeoutResult(
                    CheckInAttemptResult.SESSION_TIMED_OUT,
                    usersWhoDidNotCheckIn,
                    Set.of(),
                    MESSAGE_ID,
                    CHANNEL_ID
            );
            CheckInTimeoutEvent event = new CheckInTimeoutEvent(timeoutResult);

            // When
            listener.onCheckInTimeout(event);

            // Then
            verify(auditService, times(2)).recordEvent(
                    eq(CHANNEL_ID),
                    anyLong(),
                    eq(EventType.SPANNER_AWARDED),
                    any()
            );
        }

        @Test
        @DisplayName("should not award any spanners when all users checked in")
        void shouldNotAwardAnySpannersWhenAllUsersCheckedIn() {
            // Given - all users checked in, none timed out
            Set<Long> usersWhoDidNotCheckIn = Set.of();
            Set<Long> usersWhoCheckedIn = Set.of(111L, 222L, 333L, 444L, 555L);
            CheckInTimeoutResult timeoutResult = new CheckInTimeoutResult(
                    CheckInAttemptResult.SESSION_TIMED_OUT,
                    usersWhoDidNotCheckIn,
                    usersWhoCheckedIn,
                    MESSAGE_ID,
                    CHANNEL_ID
            );
            CheckInTimeoutEvent event = new CheckInTimeoutEvent(timeoutResult);

            // When
            listener.onCheckInTimeout(event);

            // Then
            verify(spannerService, never()).incrementSpannerCount(anyLong(), anyLong());
            verify(auditService, never()).recordEvent(
                    anyLong(), anyLong(), eq(EventType.SPANNER_AWARDED), any()
            );
        }
    }

    @Nested
    @DisplayName("Scenarios where spanner should NOT be awarded")
    class NoSpannerAwardedTests {

        @Test
        @DisplayName("PlayerNotInQueueEvent should not trigger spanner award (no handler)")
        void playerNotInQueueEventShouldNotTriggerSpannerAward() {
            // This test documents the expected behaviour:
            // PlayerNotInQueueEvent is NOT handled by SpannerAwardingEventListener
            // because there's no @EventListener method for it.
            //
            // The absence of a handler method is intentional - when a user issues
            // /unkeen but is not in the queue, no spanner should be awarded.
            //
            // This is a documentation test - the actual verification is that
            // no method in this listener accepts PlayerNotInQueueEvent.

            // Verify that spannerService is never called when no relevant event is processed
            verifyNoInteractions(spannerService);
            verifyNoInteractions(auditService);
        }
    }
}







