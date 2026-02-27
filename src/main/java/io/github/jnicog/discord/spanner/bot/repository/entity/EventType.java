package io.github.jnicog.discord.spanner.bot.repository.entity;

/**
 * Enum representing the types of events that can be audited.
 */
public enum EventType {
    // Queue events
    PLAYER_JOINED_QUEUE,
    PLAYER_LEFT_QUEUE,
    PLAYER_QUEUE_TIMEOUT,

    // Check-in events
    CHECK_IN_STARTED,
    PLAYER_CHECKED_IN,
    CHECK_IN_COMPLETED,
    CHECK_IN_CANCELLED,
    CHECK_IN_TIMEOUT,

    // Spanner events
    SPANNER_AWARDED,

    // Ten-man poll events
    TEN_MAN_POLL_CREATED,
    TEN_MAN_SIGNUP_TOGGLED,
    TEN_MAN_DATE_FULL
}

