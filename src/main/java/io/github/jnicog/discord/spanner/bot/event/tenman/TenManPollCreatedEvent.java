package io.github.jnicog.discord.spanner.bot.event.tenman;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import io.github.jnicog.discord.spanner.bot.tenman.TenManDateOptionSnapshot;

import java.time.OffsetDateTime;
import java.util.List;

public class TenManPollCreatedEvent extends AbstractCommandResult<SlashCommandContext> {

    private final long pollId;
    private final long channelId;
    private final String timeDisplay;
    private final boolean testMode;
    private final OffsetDateTime closesAt;
    private final List<TenManDateOptionSnapshot> dateOptions;

    public TenManPollCreatedEvent(SlashCommandContext context, long pollId, long channelId,
                                   String timeDisplay, boolean testMode, OffsetDateTime closesAt,
                                   List<TenManDateOptionSnapshot> dateOptions) {
        super(context);
        this.pollId = pollId;
        this.channelId = channelId;
        this.timeDisplay = timeDisplay;
        this.testMode = testMode;
        this.closesAt = closesAt;
        this.dateOptions = List.copyOf(dateOptions);
    }

    public long getPollId() { return pollId; }
    public long getChannelId() { return channelId; }
    public String getTimeDisplay() { return timeDisplay; }
    public boolean isTestMode() { return testMode; }
    public OffsetDateTime getClosesAt() { return closesAt; }
    public List<TenManDateOptionSnapshot> getDateOptions() { return dateOptions; }
}
