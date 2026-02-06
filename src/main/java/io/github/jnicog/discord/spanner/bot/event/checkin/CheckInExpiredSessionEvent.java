package io.github.jnicog.discord.spanner.bot.event.checkin;

import io.github.jnicog.discord.spanner.bot.command.InteractionContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

public class CheckInExpiredSessionEvent extends AbstractCommandResult<InteractionContext> implements CheckInEvent {

    private final long checkInMessageId;

    public CheckInExpiredSessionEvent(InteractionContext context, long checkInMessageId) {
        super(context);
        this.checkInMessageId = checkInMessageId;
    }

    @Override
    public long getCheckInMessageId() {
        return checkInMessageId;
    }
}
