package io.github.jnicog.discord.spanner.bot.event.tenman;

import io.github.jnicog.discord.spanner.bot.command.ButtonInteractionContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import io.github.jnicog.discord.spanner.bot.tenman.TenManPollSnapshot;

import java.time.LocalDate;
import java.util.List;

public class TenManDateFullEvent extends AbstractCommandResult<ButtonInteractionContext> {

    private final long pollId;
    private final long channelId;
    private final long dateOptionId;
    private final LocalDate date;
    private final List<Long> signedUpUserIds;
    private final TenManPollSnapshot updatedSnapshot;

    public TenManDateFullEvent(ButtonInteractionContext context, long pollId, long channelId,
                                long dateOptionId, LocalDate date, List<Long> signedUpUserIds,
                                TenManPollSnapshot updatedSnapshot) {
        super(context);
        this.pollId = pollId;
        this.channelId = channelId;
        this.dateOptionId = dateOptionId;
        this.date = date;
        this.signedUpUserIds = List.copyOf(signedUpUserIds);
        this.updatedSnapshot = updatedSnapshot;
    }

    public long getPollId() { return pollId; }
    public long getChannelId() { return channelId; }
    public long getDateOptionId() { return dateOptionId; }
    public LocalDate getDate() { return date; }
    public List<Long> getSignedUpUserIds() { return signedUpUserIds; }
    public TenManPollSnapshot getUpdatedSnapshot() { return updatedSnapshot; }
}
