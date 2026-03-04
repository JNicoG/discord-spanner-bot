package io.github.jnicog.discord.spanner.bot.event.tenman;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

import java.time.LocalDate;
import java.util.List;

public class TenManFillSuccessEvent extends AbstractCommandResult<SlashCommandContext> {

    private final boolean rosterRestored;
    private final long userId;
    private final long channelId;
    private final long dateOptionId;
    private final LocalDate date;
    private final String timeDisplay;
    private final int slotsNeeded;
    private final List<Long> rosterUserIds;

    public TenManFillSuccessEvent(SlashCommandContext context, boolean rosterRestored, long userId, long channelId,
                                   long dateOptionId, LocalDate date, String timeDisplay,
                                   int slotsNeeded, List<Long> rosterUserIds) {
        super(context);
        this.rosterRestored = rosterRestored;
        this.userId = userId;
        this.channelId = channelId;
        this.dateOptionId = dateOptionId;
        this.date = date;
        this.timeDisplay = timeDisplay;
        this.slotsNeeded = slotsNeeded;
        this.rosterUserIds = List.copyOf(rosterUserIds);
    }

    public boolean isRosterRestored() { return rosterRestored; }
    public long getUserId() { return userId; }
    public long getChannelId() { return channelId; }
    public long getDateOptionId() { return dateOptionId; }
    public LocalDate getDate() { return date; }
    public String getTimeDisplay() { return timeDisplay; }
    public int getSlotsNeeded() { return slotsNeeded; }
    public List<Long> getRosterUserIds() { return rosterUserIds; }
}
