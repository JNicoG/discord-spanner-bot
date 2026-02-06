package io.github.jnicog.discord.spanner.bot.checkin;

import java.util.Map;
import java.util.Set;

/**
 * Read-only operations for querying check-in session state.
 * Segregated interface for components that only need to read session state.
 */
public interface CheckInSessionReader {

    /**
     * Checks if there is an active check-in session for the given channel.
     * @param channelId The channel ID
     * @return true if there is an active session, false otherwise
     */
    boolean hasActiveSession(long channelId);

    /**
     * Gets the current check-in status snapshot for all participants.
     * @param channelId The channel ID
     * @return Map of user IDs to their check-in status
     * @throws IllegalStateException if no active session exists
     */
    Map<Long, Boolean> getUpdatedCheckInSnapshot(long channelId);

    /**
     * Gets all participants of the active session for the given channel.
     * @param channelId The channel ID
     * @return Set of user IDs in the session, or empty set if no active session
     */
    Set<Long> getSessionParticipants(long channelId);
}

