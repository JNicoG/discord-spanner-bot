package io.github.jnicog.discord.spanner.bot.leaderboard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Consumer;

/**
 * Manages active leaderboard sessions.
 * Handles session creation, lookup, and timeout scheduling.
 */
@Component
public class LeaderboardSessionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeaderboardSessionManager.class);
    private static final Duration SESSION_TIMEOUT = Duration.ofMinutes(2);

    private final Map<Long, LeaderboardSession> sessionsByMessageId = new ConcurrentHashMap<>();
    private final TaskScheduler taskScheduler;

    private Consumer<LeaderboardSession> timeoutCallback;

    public LeaderboardSessionManager(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    /**
     * Sets the callback to be invoked when a session times out.
     */
    public void setTimeoutCallback(Consumer<LeaderboardSession> callback) {
        this.timeoutCallback = callback;
    }

    /**
     * Creates a new leaderboard session.
     */
    public LeaderboardSession createSession(long messageId, long channelId, long ownerId) {
        LeaderboardSession session = new LeaderboardSession(messageId, channelId, ownerId);
        sessionsByMessageId.put(messageId, session);

        scheduleTimeout(session);

        LOGGER.debug("Created leaderboard session for message {} in channel {} for user {}",
                messageId, channelId, ownerId);

        return session;
    }

    /**
     * Gets a session by message ID.
     */
    public Optional<LeaderboardSession> getSession(long messageId) {
        return Optional.ofNullable(sessionsByMessageId.get(messageId));
    }

    /**
     * Removes a session.
     */
    public void removeSession(long messageId) {
        LeaderboardSession session = sessionsByMessageId.remove(messageId);
        if (session != null) {
            session.cancelTimeout();
            LOGGER.debug("Removed leaderboard session for message {}", messageId);
        }
    }

    /**
     * Checks if a user is authorised to interact with a leaderboard message.
     */
    public boolean isAuthorised(long messageId, long userId) {
        return getSession(messageId)
                .map(session -> session.isOwner(userId))
                .orElse(false);
    }

    /**
     * Resets the timeout for a session (called on valid interaction).
     */
    public void resetTimeout(long messageId) {
        getSession(messageId).ifPresent(session -> {
            session.cancelTimeout();
            scheduleTimeout(session);
            LOGGER.debug("Reset timeout for leaderboard session {}", messageId);
        });
    }

    private void scheduleTimeout(LeaderboardSession session) {
        ScheduledFuture<?> future = taskScheduler.schedule(
                () -> handleTimeout(session),
                Instant.now().plus(SESSION_TIMEOUT)
        );
        session.setTimeoutFuture(future);
    }

    private void handleTimeout(LeaderboardSession session) {
        LOGGER.debug("Leaderboard session {} timed out", session.getMessageId());
        sessionsByMessageId.remove(session.getMessageId());

        if (timeoutCallback != null) {
            timeoutCallback.accept(session);
        }
    }
}

