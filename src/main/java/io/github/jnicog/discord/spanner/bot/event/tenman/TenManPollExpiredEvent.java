package io.github.jnicog.discord.spanner.bot.event.tenman;

import io.github.jnicog.discord.spanner.bot.tenman.TenManPollSnapshot;

public class TenManPollExpiredEvent {

    private final TenManPollSnapshot snapshot;

    public TenManPollExpiredEvent(TenManPollSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public TenManPollSnapshot getSnapshot() {
        return snapshot;
    }
}
