package io.github.jnicog.discord.spanner.bot.event;

public enum CheckInResult {
    CHECKED_IN,
    ALREADY_CHECKED_IN,
    UNAUTHORISED,
    NO_ACTIVE_SESSION,
    SESSION_COMPLETED,
    SESSION_CANCELLED
}
