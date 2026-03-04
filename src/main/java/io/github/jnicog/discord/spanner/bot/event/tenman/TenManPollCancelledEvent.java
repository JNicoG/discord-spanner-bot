package io.github.jnicog.discord.spanner.bot.event.tenman;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

public class TenManPollCancelledEvent extends AbstractCommandResult<SlashCommandContext> {

    private final long channelId;
    private final Long discordMessageId; // null if the poll message was never posted

    public TenManPollCancelledEvent(SlashCommandContext context, long channelId, Long discordMessageId) {
        super(context);
        this.channelId = channelId;
        this.discordMessageId = discordMessageId;
    }

    public long getChannelId() { return channelId; }
    public Long getDiscordMessageId() { return discordMessageId; }
}
