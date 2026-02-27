package io.github.jnicog.discord.spanner.bot.repository.entity;

public enum TenManPollStatus {
    ACTIVE,
    LOCKED,  // date reached capacity; sign-off and fill still allowed
    CLOSED   // expired or cancelled; no further interactions
}
