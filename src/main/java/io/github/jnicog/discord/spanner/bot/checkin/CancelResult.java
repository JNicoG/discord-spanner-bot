package io.github.jnicog.discord.spanner.bot.checkin;

import java.util.Set;

/**
 * Result of a check-in cancellation operation.
 */
public record CancelResult(
        CheckInAttemptResult result,
        Set<Long> remainingUsers,
        long messageId
) {
    public static CancelResult noActiveSession() {
        return new CancelResult(CheckInAttemptResult.NO_ACTIVE_SESSION, Set.of(), -1);
    }

    public static CancelResult unauthorised() {
        return new CancelResult(CheckInAttemptResult.UNAUTHORISED, Set.of(), -1);
    }

    public static CancelResult cancelled(Set<Long> remainingUsers, long messageId) {
        return new CancelResult(CheckInAttemptResult.SESSION_CANCELLED, remainingUsers, messageId);
    }
}

