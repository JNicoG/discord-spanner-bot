package io.github.jnicog.discord.spanner.bot.event.checkin;

import io.github.jnicog.discord.spanner.bot.command.ButtonContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

public class NoActiveSessionEvent extends AbstractCommandResult<ButtonContext> {

    public NoActiveSessionEvent(ButtonContext commandContext) {
        super(commandContext);
    }

}
