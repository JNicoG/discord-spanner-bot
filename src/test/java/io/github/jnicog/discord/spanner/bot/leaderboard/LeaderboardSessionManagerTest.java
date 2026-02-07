package io.github.jnicog.discord.spanner.bot.leaderboard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LeaderboardSessionManager")
class LeaderboardSessionManagerTest {

    private static final long MESSAGE_ID = 111111111L;
    private static final long CHANNEL_ID = 222222222L;
    private static final long OWNER_ID = 333333333L;
    private static final long OTHER_USER_ID = 444444444L;

    @Mock
    private TaskScheduler taskScheduler;

    @Mock
    @SuppressWarnings("rawtypes")
    private ScheduledFuture scheduledFuture;

    private LeaderboardSessionManager sessionManager;

    @BeforeEach
    void setUp() {
        sessionManager = new LeaderboardSessionManager(taskScheduler);
    }

    @SuppressWarnings("unchecked")
    private void stubScheduler() {
        when(taskScheduler.schedule(any(Runnable.class), any(Instant.class))).thenReturn(scheduledFuture);
    }

    @Nested
    @DisplayName("When creating a session")
    class CreateSessionTests {

        @Test
        @DisplayName("should create session with correct properties")
        void shouldCreateSessionWithCorrectProperties() {
            // Given
            stubScheduler();

            // When
            LeaderboardSession session = sessionManager.createSession(MESSAGE_ID, CHANNEL_ID, OWNER_ID);

            // Then
            assertEquals(MESSAGE_ID, session.getMessageId());
            assertEquals(CHANNEL_ID, session.getChannelId());
            assertEquals(OWNER_ID, session.getOwnerId());
            assertEquals(1, session.getCurrentPage());
        }

        @Test
        @DisplayName("should schedule timeout")
        void shouldScheduleTimeout() {
            // Given
            stubScheduler();

            // When
            sessionManager.createSession(MESSAGE_ID, CHANNEL_ID, OWNER_ID);

            // Then
            verify(taskScheduler).schedule(any(Runnable.class), any(Instant.class));
        }

        @Test
        @DisplayName("should be retrievable after creation")
        void shouldBeRetrievableAfterCreation() {
            // Given
            stubScheduler();

            // When
            sessionManager.createSession(MESSAGE_ID, CHANNEL_ID, OWNER_ID);

            // Then
            Optional<LeaderboardSession> retrieved = sessionManager.getSession(MESSAGE_ID);
            assertTrue(retrieved.isPresent());
            assertEquals(MESSAGE_ID, retrieved.get().getMessageId());
        }
    }

    @Nested
    @DisplayName("When checking authorisation")
    class AuthorisationTests {

        @Test
        @DisplayName("should return true for session owner")
        void shouldReturnTrueForSessionOwner() {
            // Given
            stubScheduler();
            sessionManager.createSession(MESSAGE_ID, CHANNEL_ID, OWNER_ID);

            // When
            boolean isAuthorised = sessionManager.isAuthorised(MESSAGE_ID, OWNER_ID);

            // Then
            assertTrue(isAuthorised);
        }

        @Test
        @DisplayName("should return false for non-owner")
        void shouldReturnFalseForNonOwner() {
            // Given
            stubScheduler();
            sessionManager.createSession(MESSAGE_ID, CHANNEL_ID, OWNER_ID);

            // When
            boolean isAuthorised = sessionManager.isAuthorised(MESSAGE_ID, OTHER_USER_ID);

            // Then
            assertFalse(isAuthorised);
        }

        @Test
        @DisplayName("should return false for unknown message")
        void shouldReturnFalseForUnknownMessage() {
            // When - no session created, no stub needed
            boolean isAuthorised = sessionManager.isAuthorised(999999L, OWNER_ID);

            // Then
            assertFalse(isAuthorised);
        }
    }

    @Nested
    @DisplayName("When removing a session")
    class RemoveSessionTests {

        @Test
        @DisplayName("should remove session from manager")
        void shouldRemoveSessionFromManager() {
            // Given
            stubScheduler();
            sessionManager.createSession(MESSAGE_ID, CHANNEL_ID, OWNER_ID);

            // When
            sessionManager.removeSession(MESSAGE_ID);

            // Then
            Optional<LeaderboardSession> retrieved = sessionManager.getSession(MESSAGE_ID);
            assertTrue(retrieved.isEmpty());
        }

        @Test
        @DisplayName("should cancel timeout future when removing")
        void shouldCancelTimeoutFutureWhenRemoving() {
            // Given
            stubScheduler();
            sessionManager.createSession(MESSAGE_ID, CHANNEL_ID, OWNER_ID);

            // When
            sessionManager.removeSession(MESSAGE_ID);

            // Then
            verify(scheduledFuture).cancel(false);
        }

        @Test
        @DisplayName("should handle removing non-existent session gracefully")
        void shouldHandleRemovingNonExistentSessionGracefully() {
            // When/Then - no session exists, no stub needed
            assertDoesNotThrow(() -> sessionManager.removeSession(999999L));
        }
    }

    @Nested
    @DisplayName("When resetting timeout")
    class ResetTimeoutTests {

        @Test
        @DisplayName("should cancel existing timeout and schedule new one")
        @SuppressWarnings("unchecked")
        void shouldCancelExistingTimeoutAndScheduleNewOne() {
            // Given
            stubScheduler();
            sessionManager.createSession(MESSAGE_ID, CHANNEL_ID, OWNER_ID);
            reset(taskScheduler);
            when(taskScheduler.schedule(any(Runnable.class), any(Instant.class))).thenReturn(scheduledFuture);

            // When
            sessionManager.resetTimeout(MESSAGE_ID);

            // Then
            verify(scheduledFuture).cancel(false);
            verify(taskScheduler).schedule(any(Runnable.class), any(Instant.class));
        }
    }
}



