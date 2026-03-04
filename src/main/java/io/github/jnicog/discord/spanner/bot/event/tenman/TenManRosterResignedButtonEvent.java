package io.github.jnicog.discord.spanner.bot.event.tenman;

import io.github.jnicog.discord.spanner.bot.command.ButtonInteractionContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import io.github.jnicog.discord.spanner.bot.tenman.TenManPollSnapshot;

import java.time.LocalDate;

public class TenManRosterResignedButtonEvent extends AbstractCommandResult<ButtonInteractionContext> {

    private final long userId;
    private final long channelId;
    private final TenManPollSnapshot updatedSnapshot;
    private final LocalDate lockedDate;
    private final int slotsNeeded;

    public TenManRosterResignedButtonEvent(ButtonInteractionContext context, long userId, long channelId,
                                           TenManPollSnapshot updatedSnapshot, LocalDate lockedDate, int slotsNeeded) {
        super(context);
        this.userId = userId;
        this.channelId = channelId;
        this.updatedSnapshot = updatedSnapshot;
        this.lockedDate = lockedDate;
        this.slotsNeeded = slotsNeeded;
    }

    public long getUserId() { return userId; }
    public long getChannelId() { return channelId; }
    public TenManPollSnapshot getUpdatedSnapshot() { return updatedSnapshot; }
    public LocalDate getLockedDate() { return lockedDate; }
    public int getSlotsNeeded() { return slotsNeeded; }
}
