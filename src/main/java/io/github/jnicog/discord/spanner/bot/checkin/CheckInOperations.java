package io.github.jnicog.discord.spanner.bot.checkin;

/**
 * Operations for user interactions with check-in sessions.
 * Segregated interface for components that handle user check-in/cancel actions.
 */
public interface CheckInOperations {

    /**
     * Processes a user's check-in attempt.
     * @param channelId The channel ID
     * @param userId The user ID attempting to check in
     * @param messageId The message ID of the check-in message (for validation)
     * @return Result indicating the outcome of the check-in attempt
     */
    CheckInAttemptResult userCheckIn(long channelId, long userId, long messageId);

    /**
     * Processes a user's cancellation of the check-in session.
     * @param channelId The channel ID
     * @param userId The user ID requesting cancellation
     * @return Result indicating the outcome of the cancellation attempt
     */
    CheckInAttemptResult userCancel(long channelId, long userId);

    /**
     * Cancels the check-in session and returns the remaining users (excluding the cancelling user).
     * @param channelId The channel ID
     * @param cancellingUserId The user ID of the person cancelling
     * @return CancelResult containing the result and remaining users
     */
    CancelResult cancelAndGetRemainingUsers(long channelId, long cancellingUserId);
}

