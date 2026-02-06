package io.github.jnicog.discord.spanner.bot.event.checkin;

import io.github.jnicog.discord.spanner.bot.command.InteractionContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

import java.util.Map;

public class PlayerCheckInEvent extends AbstractCommandResult<InteractionContext> implements CheckInEvent {

    private long checkInMessageId;
    private final Map<Long, Boolean> updatedCheckInSnapshot;

    public PlayerCheckInEvent(InteractionContext context,
                              Map<Long, Boolean> updatedCheckInSnapshot,
                              long checkInMessageId) {
        super(context);
        this.updatedCheckInSnapshot = updatedCheckInSnapshot;
        this.checkInMessageId = checkInMessageId;
    }

    @Override
    public long getCheckInMessageId() {
        return checkInMessageId;
    }

    public Map<Long, Boolean> getUpdatedCheckInSnapshot() {
        return updatedCheckInSnapshot;
    }
}
