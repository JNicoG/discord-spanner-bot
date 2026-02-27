package io.github.jnicog.discord.spanner.bot.event.tenman;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

import java.time.LocalDate;

public class TenManRosterResignedSlashEvent extends AbstractCommandResult<SlashCommandContext> {

    private final long userId;
    private final long channelId;
    private final long dateOptionId;
    private final LocalDate lockedDate;
    private final String timeDisplay;
    private final int slotsNeeded;

    public TenManRosterResignedSlashEvent(SlashCommandContext context, long userId, long channelId,
                                          long dateOptionId, LocalDate lockedDate, String timeDisplay, int slotsNeeded) {
        super(context);
        this.userId = userId;
        this.channelId = channelId;
        this.dateOptionId = dateOptionId;
        this.lockedDate = lockedDate;
        this.timeDisplay = timeDisplay;
        this.slotsNeeded = slotsNeeded;
    }

    public long getUserId() { return userId; }
    public long getChannelId() { return channelId; }
    public long getDateOptionId() { return dateOptionId; }
    public LocalDate getLockedDate() { return lockedDate; }
    public String getTimeDisplay() { return timeDisplay; }
    public int getSlotsNeeded() { return slotsNeeded; }
}
