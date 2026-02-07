package io.github.jnicog.discord.spanner.bot.checkin;

import java.util.Set;

/**
 * Result of a check-in session timeout.
 *
 * @param result The outcome of the timeout operation
 * @param usersWhoDidNotCheckIn Users who failed to check in before the timeout
 * @param usersWhoCheckedIn Users who successfully checked in before the timeout
 * @param messageId The message ID of the check-in message
 * @param channelId The channel ID where the session was active
 */
public record CheckInTimeoutResult(
        CheckInAttemptResult result,
        Set<Long> usersWhoDidNotCheckIn,
        Set<Long> usersWhoCheckedIn,
        long messageId,
        long channelId
) {
    public static CheckInTimeoutResult noActiveSession(long channelId) {
        return new CheckInTimeoutResult(
                CheckInAttemptResult.NO_ACTIVE_SESSION,
                Set.of(),
                Set.of(),
                -1,
                channelId
        );
    }

    public static CheckInTimeoutResult timedOut(
            Set<Long> usersWhoDidNotCheckIn,
            Set<Long> usersWhoCheckedIn,
            long messageId,
            long channelId) {
        return new CheckInTimeoutResult(
                CheckInAttemptResult.SESSION_TIMED_OUT,
                Set.copyOf(usersWhoDidNotCheckIn),
                Set.copyOf(usersWhoCheckedIn),
                messageId,
                channelId
        );
    }
}

