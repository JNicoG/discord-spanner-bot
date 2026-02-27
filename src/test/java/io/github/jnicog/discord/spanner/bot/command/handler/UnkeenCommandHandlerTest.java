package io.github.jnicog.discord.spanner.bot.command.handler;

import io.github.jnicog.discord.spanner.bot.checkin.CancelResult;
import io.github.jnicog.discord.spanner.bot.checkin.CheckInService;
import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import io.github.jnicog.discord.spanner.bot.event.checkin.UnkeenDuringCheckInEvent;
import io.github.jnicog.discord.spanner.bot.event.queue.PlayerLeftQueueEvent;
import io.github.jnicog.discord.spanner.bot.event.queue.PlayerNotInQueueEvent;
import io.github.jnicog.discord.spanner.bot.queue.QueueOutcome;
import io.github.jnicog.discord.spanner.bot.queue.QueueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UnkeenCommandHandler")
class UnkeenCommandHandlerTest {

    private static final long USER_ID = 123456789L;
    private static final long OTHER_USER_ID = 111111111L;
    private static final long CHANNEL_ID = 987654321L;
    private static final long MESSAGE_ID = 555555555L;
    private static final int MAX_QUEUE_SIZE = 5;

    @Mock
    private QueueService queueService;

    @Mock
    private CheckInService checkInService;

    private UnkeenCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new UnkeenCommandHandler(queueService, checkInService);
    }

    private SlashCommandContext createContext() {
        return new SlashCommandContext(
                OffsetDateTime.now(),
                "unkeen",
                USER_ID,
                "testuser",
                CHANNEL_ID,
                Map.of()
        );
    }

    @Nested
    @DisplayName("When user is in queue and no check-in session is active")
    class UserInQueueNoCheckIn {

        @Test
        @DisplayName("should return PlayerLeftQueueEvent when user successfully leaves queue")
        void shouldReturnPlayerLeftQueueEventWhenUserLeavesQueue() {
            // Given
            when(checkInService.hasActiveSession(CHANNEL_ID)).thenReturn(false);
            when(queueService.leaveQueue(USER_ID, CHANNEL_ID)).thenReturn(QueueOutcome.DEQUEUED);
            when(queueService.showQueue(CHANNEL_ID)).thenReturn(Set.of(OTHER_USER_ID));
            when(queueService.showMaxQueueSize(CHANNEL_ID)).thenReturn(MAX_QUEUE_SIZE);

            // When
            AbstractCommandResult<?> result = handler.handleCommand(createContext());

            // Then
            assertInstanceOf(PlayerLeftQueueEvent.class, result);
            PlayerLeftQueueEvent event = (PlayerLeftQueueEvent) result;
            assertEquals(USER_ID, event.getContext().userId());
            assertEquals(CHANNEL_ID, event.getContext().channelId());
            assertTrue(event.getUpdatedQueueSnapshot().contains(OTHER_USER_ID));
        }

        @Test
        @DisplayName("PlayerLeftQueueEvent should trigger spanner award")
        void playerLeftQueueEventShouldTriggerSpannerAward() {
            // Given
            when(checkInService.hasActiveSession(CHANNEL_ID)).thenReturn(false);
            when(queueService.leaveQueue(USER_ID, CHANNEL_ID)).thenReturn(QueueOutcome.DEQUEUED);
            when(queueService.showQueue(CHANNEL_ID)).thenReturn(Set.of());
            when(queueService.showMaxQueueSize(CHANNEL_ID)).thenReturn(MAX_QUEUE_SIZE);

            // When
            AbstractCommandResult<?> result = handler.handleCommand(createContext());

            // Then
            assertInstanceOf(PlayerLeftQueueEvent.class, result);
            // The SpannerAwardingEventListener will receive this event and award a spanner
            // This is verified in SpannerAwardingEventListenerTest
        }
    }

    @Nested
    @DisplayName("When user is NOT in queue")
    class UserNotInQueue {

        @Test
        @DisplayName("should return PlayerNotInQueueEvent when user is not in queue")
        void shouldReturnPlayerNotInQueueEventWhenUserNotInQueue() {
            // Given
            when(checkInService.hasActiveSession(CHANNEL_ID)).thenReturn(false);
            when(queueService.leaveQueue(USER_ID, CHANNEL_ID)).thenReturn(QueueOutcome.NOT_IN_QUEUE);
            when(queueService.showQueue(CHANNEL_ID)).thenReturn(Set.of(OTHER_USER_ID));
            when(queueService.showMaxQueueSize(CHANNEL_ID)).thenReturn(MAX_QUEUE_SIZE);

            // When
            AbstractCommandResult<?> result = handler.handleCommand(createContext());

            // Then
            assertInstanceOf(PlayerNotInQueueEvent.class, result);
        }

        @Test
        @DisplayName("PlayerNotInQueueEvent should NOT trigger spanner award")
        void playerNotInQueueEventShouldNotTriggerSpannerAward() {
            // Given
            when(checkInService.hasActiveSession(CHANNEL_ID)).thenReturn(false);
            when(queueService.leaveQueue(USER_ID, CHANNEL_ID)).thenReturn(QueueOutcome.NOT_IN_QUEUE);
            when(queueService.showQueue(CHANNEL_ID)).thenReturn(Set.of());
            when(queueService.showMaxQueueSize(CHANNEL_ID)).thenReturn(MAX_QUEUE_SIZE);

            // When
            AbstractCommandResult<?> result = handler.handleCommand(createContext());

            // Then
            assertInstanceOf(PlayerNotInQueueEvent.class, result);
            // No spanner is awarded because SpannerAwardingEventListener
            // does not listen for PlayerNotInQueueEvent
        }
    }

    @Nested
    @DisplayName("When check-in session is active and user is a participant")
    class ActiveCheckInUserIsParticipant {

        @Test
        @DisplayName("should return UnkeenDuringCheckInEvent when user cancels check-in")
        void shouldReturnUnkeenDuringCheckInEventWhenUserCancelsCheckIn() {
            // Given
            when(checkInService.hasActiveSession(CHANNEL_ID)).thenReturn(true);
            when(checkInService.getSessionParticipants(CHANNEL_ID)).thenReturn(Set.of(USER_ID, OTHER_USER_ID));
            when(checkInService.cancelAndGetRemainingUsers(CHANNEL_ID, USER_ID))
                    .thenReturn(CancelResult.cancelled(Set.of(OTHER_USER_ID), MESSAGE_ID));
            when(queueService.showQueue(CHANNEL_ID)).thenReturn(Set.of(OTHER_USER_ID));
            when(queueService.showMaxQueueSize(CHANNEL_ID)).thenReturn(MAX_QUEUE_SIZE);

            // When
            AbstractCommandResult<?> result = handler.handleCommand(createContext());

            // Then
            assertInstanceOf(UnkeenDuringCheckInEvent.class, result);
            UnkeenDuringCheckInEvent event = (UnkeenDuringCheckInEvent) result;
            assertEquals(USER_ID, event.getContext().userId());
            assertTrue(event.getRemainingUsersInQueue().contains(OTHER_USER_ID));
            assertEquals(MESSAGE_ID, event.getCheckInMessageId());
        }

        @Test
        @DisplayName("should remove user from queue when cancelling check-in")
        void shouldRemoveUserFromQueueWhenCancellingCheckIn() {
            // Given
            when(checkInService.hasActiveSession(CHANNEL_ID)).thenReturn(true);
            when(checkInService.getSessionParticipants(CHANNEL_ID)).thenReturn(Set.of(USER_ID));
            when(checkInService.cancelAndGetRemainingUsers(CHANNEL_ID, USER_ID))
                    .thenReturn(CancelResult.cancelled(Set.of(), MESSAGE_ID));
            when(queueService.showQueue(CHANNEL_ID)).thenReturn(Set.of());
            when(queueService.showMaxQueueSize(CHANNEL_ID)).thenReturn(MAX_QUEUE_SIZE);

            // When
            handler.handleCommand(createContext());

            // Then
            verify(queueService).leaveQueue(USER_ID, CHANNEL_ID);
        }

        @Test
        @DisplayName("UnkeenDuringCheckInEvent should trigger spanner award")
        void unkeenDuringCheckInEventShouldTriggerSpannerAward() {
            // Given
            when(checkInService.hasActiveSession(CHANNEL_ID)).thenReturn(true);
            when(checkInService.getSessionParticipants(CHANNEL_ID)).thenReturn(Set.of(USER_ID));
            when(checkInService.cancelAndGetRemainingUsers(CHANNEL_ID, USER_ID))
                    .thenReturn(CancelResult.cancelled(Set.of(), MESSAGE_ID));
            when(queueService.showQueue(CHANNEL_ID)).thenReturn(Set.of());
            when(queueService.showMaxQueueSize(CHANNEL_ID)).thenReturn(MAX_QUEUE_SIZE);

            // When
            AbstractCommandResult<?> result = handler.handleCommand(createContext());

            // Then
            assertInstanceOf(UnkeenDuringCheckInEvent.class, result);
            // The SpannerAwardingEventListener will receive this event and award a spanner
        }
    }

    @Nested
    @DisplayName("When check-in session is active but user is NOT a participant")
    class ActiveCheckInUserNotParticipant {

        @Test
        @DisplayName("should return PlayerNotInQueueEvent when user is not in queue or session")
        void shouldReturnPlayerNotInQueueEventWhenUserNotInQueueOrSession() {
            // Given - check-in is active, but user is not a participant and not in queue
            when(checkInService.hasActiveSession(CHANNEL_ID)).thenReturn(true);
            when(checkInService.getSessionParticipants(CHANNEL_ID)).thenReturn(Set.of(OTHER_USER_ID));
            when(queueService.leaveQueue(USER_ID, CHANNEL_ID)).thenReturn(QueueOutcome.NOT_IN_QUEUE);
            when(queueService.showQueue(CHANNEL_ID)).thenReturn(Set.of(OTHER_USER_ID));
            when(queueService.showMaxQueueSize(CHANNEL_ID)).thenReturn(MAX_QUEUE_SIZE);

            // When
            AbstractCommandResult<?> result = handler.handleCommand(createContext());

            // Then
            assertInstanceOf(PlayerNotInQueueEvent.class, result);
        }

        @Test
        @DisplayName("should NOT award spanner when user is not a participant")
        void shouldNotAwardSpannerWhenUserNotAParticipant() {
            // Given
            when(checkInService.hasActiveSession(CHANNEL_ID)).thenReturn(true);
            when(checkInService.getSessionParticipants(CHANNEL_ID)).thenReturn(Set.of(OTHER_USER_ID));
            when(queueService.leaveQueue(USER_ID, CHANNEL_ID)).thenReturn(QueueOutcome.NOT_IN_QUEUE);
            when(queueService.showQueue(CHANNEL_ID)).thenReturn(Set.of());
            when(queueService.showMaxQueueSize(CHANNEL_ID)).thenReturn(MAX_QUEUE_SIZE);

            // When
            AbstractCommandResult<?> result = handler.handleCommand(createContext());

            // Then
            assertInstanceOf(PlayerNotInQueueEvent.class, result);
            // No spanner is awarded - PlayerNotInQueueEvent is not handled by SpannerAwardingEventListener
        }

        @Test
        @DisplayName("should NOT cancel check-in session when user is not a participant")
        void shouldNotCancelCheckInSessionWhenUserNotAParticipant() {
            // Given
            when(checkInService.hasActiveSession(CHANNEL_ID)).thenReturn(true);
            when(checkInService.getSessionParticipants(CHANNEL_ID)).thenReturn(Set.of(OTHER_USER_ID));
            when(queueService.leaveQueue(USER_ID, CHANNEL_ID)).thenReturn(QueueOutcome.NOT_IN_QUEUE);
            when(queueService.showQueue(CHANNEL_ID)).thenReturn(Set.of());
            when(queueService.showMaxQueueSize(CHANNEL_ID)).thenReturn(MAX_QUEUE_SIZE);

            // When
            handler.handleCommand(createContext());

            // Then
            verify(checkInService, never()).cancelAndGetRemainingUsers(anyLong(), anyLong());
        }
    }
}








