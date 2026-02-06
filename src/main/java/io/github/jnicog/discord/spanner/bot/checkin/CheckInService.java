package io.github.jnicog.discord.spanner.bot.checkin;

import java.util.Map;
import java.util.Set;

public interface CheckInService {
    void startCheckIn(long channelId, Set<Long> queueSnapshot);
    void registerMessageId(long channelId, long messageId);
    CheckInAttemptResult userCheckIn(long channelId, long userId, long messageId);
    CheckInAttemptResult userCancel(long channelId, long userId);
    CheckInAttemptResult completeSession(long channelId);
    CheckInAttemptResult timeoutSession(long channelId);
    boolean hasActiveSession(long channelId);
    Map<Long, Boolean> getUpdatedCheckInSnapshot(long channelId);

    /**
     * Gets all participants of the active session for the given channel.
     * @param channelId The channel ID
     * @return Set of user IDs in the session, or empty set if no active session
     */
    Set<Long> getSessionParticipants(long channelId);

    /**
     * Cancels the check-in session and returns the remaining users (excluding the cancelling user).
     * @param channelId The channel ID
     * @param cancellingUserId The user ID of the person cancelling
     * @return CancelResult containing the result and remaining users
     */
    CancelResult cancelAndGetRemainingUsers(long channelId, long cancellingUserId);

    /**
     * Gets the message ID for the active check-in session.
     * @param channelId The channel ID
     * @return The message ID, or -1 if no active session
     */
    long getSessionMessageId(long channelId);
}
