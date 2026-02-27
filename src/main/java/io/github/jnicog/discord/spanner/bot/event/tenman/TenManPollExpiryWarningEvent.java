package io.github.jnicog.discord.spanner.bot.event.tenman;

import io.github.jnicog.discord.spanner.bot.tenman.TenManPollSnapshot;

public class TenManPollExpiryWarningEvent {

    private final TenManPollSnapshot snapshot;

    public TenManPollExpiryWarningEvent(TenManPollSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public TenManPollSnapshot getSnapshot() {
        return snapshot;
    }
}
