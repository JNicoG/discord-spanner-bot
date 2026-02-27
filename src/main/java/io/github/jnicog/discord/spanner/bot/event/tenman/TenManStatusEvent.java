package io.github.jnicog.discord.spanner.bot.event.tenman;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import io.github.jnicog.discord.spanner.bot.tenman.TenManPollSnapshot;

public class TenManStatusEvent extends AbstractCommandResult<SlashCommandContext> {

    private final TenManPollSnapshot snapshot;

    public TenManStatusEvent(SlashCommandContext context, TenManPollSnapshot snapshot) {
        super(context);
        this.snapshot = snapshot;
    }

    public TenManPollSnapshot getSnapshot() {
        return snapshot;
    }
}
