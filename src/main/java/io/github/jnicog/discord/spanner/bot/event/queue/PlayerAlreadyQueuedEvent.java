package io.github.jnicog.discord.spanner.bot.event.queue;

import io.github.jnicog.discord.spanner.bot.command.CommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

/**
 * @deprecated Use {@link PlayerAlreadyQueuedEventV2} instead.
 */
@Deprecated
public class PlayerAlreadyQueuedEvent extends AbstractCommandResult<CommandContext> {

    public PlayerAlreadyQueuedEvent(CommandContext commandContext) {
        super(commandContext);
    }

}
