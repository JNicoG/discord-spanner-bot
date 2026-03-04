package io.github.jnicog.discord.spanner.bot.event.tenman;

import io.github.jnicog.discord.spanner.bot.command.ButtonInteractionContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import io.github.jnicog.discord.spanner.bot.tenman.TenManPollSnapshot;

public class TenManSignupToggledEvent extends AbstractCommandResult<ButtonInteractionContext> {

    private final long pollId;
    private final long channelId;
    private final long discordMessageId;
    private final long userId;
    private final boolean added;
    private final long dateOptionId;
    private final TenManPollSnapshot updatedSnapshot;

    public TenManSignupToggledEvent(ButtonInteractionContext context, long pollId, long channelId,
                                     long discordMessageId, long userId, boolean added,
                                     long dateOptionId, TenManPollSnapshot updatedSnapshot) {
        super(context);
        this.pollId = pollId;
        this.channelId = channelId;
        this.discordMessageId = discordMessageId;
        this.userId = userId;
        this.added = added;
        this.dateOptionId = dateOptionId;
        this.updatedSnapshot = updatedSnapshot;
    }

    public long getPollId() { return pollId; }
    public long getChannelId() { return channelId; }
    public long getDiscordMessageId() { return discordMessageId; }
    public long getUserId() { return userId; }
    public boolean isAdded() { return added; }
    public long getDateOptionId() { return dateOptionId; }
    public TenManPollSnapshot getUpdatedSnapshot() { return updatedSnapshot; }
}
