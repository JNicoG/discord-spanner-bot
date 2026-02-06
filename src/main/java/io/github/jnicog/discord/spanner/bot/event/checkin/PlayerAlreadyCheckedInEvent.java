package io.github.jnicog.discord.spanner.bot.event.checkin;

import io.github.jnicog.discord.spanner.bot.command.ButtonContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

public class PlayerAlreadyCheckedInEvent extends AbstractCommandResult<ButtonContext> {

    public PlayerAlreadyCheckedInEvent(ButtonContext context) {
        super(context);
    }

}
