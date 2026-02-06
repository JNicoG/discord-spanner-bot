package io.github.jnicog.discord.spanner.bot.event.checkin;

import io.github.jnicog.discord.spanner.bot.command.ButtonContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

public class PlayerCheckInEvent extends AbstractCommandResult<ButtonContext> implements CheckInEvent {

    protected PlayerCheckInEvent(ButtonContext context) {
        super(context);
    }

    @Override
    public long getCheckInMessageId() {
        return 0;
    }
}
