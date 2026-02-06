package io.github.jnicog.discord.spanner.bot.event.queue;

import io.github.jnicog.discord.spanner.bot.command.ButtonContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

public class CheckInActiveEvent extends AbstractCommandResult<ButtonContext> {

    public CheckInActiveEvent(ButtonContext commandContext) {
        super(commandContext);
    }

}
