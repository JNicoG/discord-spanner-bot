package io.github.jnicog.discord.spanner.bot.event.queue;

import io.github.jnicog.discord.spanner.bot.command.CommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

public class PlayerNotInQueueEvent extends AbstractCommandResult {

    public PlayerNotInQueueEvent(CommandContext commandContext) {
        super(commandContext);
    }

}
