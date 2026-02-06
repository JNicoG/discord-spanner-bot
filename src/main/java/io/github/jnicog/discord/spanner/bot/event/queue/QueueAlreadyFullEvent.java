package io.github.jnicog.discord.spanner.bot.event.queue;

import io.github.jnicog.discord.spanner.bot.command.CommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

/**
 * @deprecated Use {@link QueueAlreadyFullEventV2} instead.
 */
@Deprecated
public class QueueAlreadyFullEvent extends AbstractCommandResult<CommandContext> {

    public QueueAlreadyFullEvent(CommandContext commandContext) {
        super(commandContext);
    }

}
