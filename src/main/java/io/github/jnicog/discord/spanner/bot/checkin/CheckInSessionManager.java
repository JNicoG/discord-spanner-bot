package io.github.jnicog.discord.spanner.bot.checkin;

import java.util.Set;

/**
 * Operations for managing check-in session lifecycle.
 * Segregated interface for components that manage session creation and completion.
 */
public interface CheckInSessionManager {

    /**
     * Starts a new check-in session for the given channel.
     * @param channelId The channel ID
     * @param queueSnapshot The set of user IDs participating in the check-in
     */
    void startCheckIn(long channelId, Set<Long> queueSnapshot);

    /**
     * Registers the message ID for the check-in session.
     * @param channelId The channel ID
     * @param messageId The message ID of the check-in message
     */
    void registerMessageId(long channelId, long messageId);

    /**
     * Manually completes the check-in session.
     * @param channelId The channel ID
     * @return Result indicating the outcome
     */
    CheckInAttemptResult completeSession(long channelId);

    /**
     * Handles session timeout.
     * @param channelId The channel ID
     * @return Result indicating the outcome
     */
    CheckInAttemptResult timeoutSession(long channelId);
}

