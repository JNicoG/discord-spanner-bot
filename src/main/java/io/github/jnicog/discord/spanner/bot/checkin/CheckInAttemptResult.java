package io.github.jnicog.discord.spanner.bot.checkin;

public enum CheckInAttemptResult {
    CHECKED_IN,
    ALREADY_CHECKED_IN,
    UNAUTHORISED,
    NO_ACTIVE_SESSION,
    EXPIRED_SESSION,
    SESSION_COMPLETED,
    SESSION_CANCELLED,
    SESSION_TIMED_OUT
}
