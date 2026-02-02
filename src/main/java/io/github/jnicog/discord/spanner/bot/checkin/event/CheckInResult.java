package io.github.jnicog.discord.spanner.bot.checkin.event;

public enum CheckInResult {
    CHECKED_IN,
    ALREADY_CHECKED_IN,
    NO_ACTIVE_SESSION,
    COMPLETE_CHECK_IN
}
