package io.github.jnicog.discord.spanner.bot.event.checkin;

import io.github.jnicog.discord.spanner.bot.command.CommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

public class UnauthorisedCheckInEvent extends AbstractCommandResult {

    public UnauthorisedCheckInEvent(CommandContext commandContext) {
        super(commandContext);
    }

}
