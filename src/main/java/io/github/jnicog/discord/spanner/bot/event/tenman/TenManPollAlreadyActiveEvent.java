package io.github.jnicog.discord.spanner.bot.event.tenman;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

public class TenManPollAlreadyActiveEvent extends AbstractCommandResult<SlashCommandContext> {

    private final long channelId;

    public TenManPollAlreadyActiveEvent(SlashCommandContext context, long channelId) {
        super(context);
        this.channelId = channelId;
    }

    public long getChannelId() { return channelId; }
}
