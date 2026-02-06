package io.github.jnicog.discord.spanner.bot.event.checkin;

import io.github.jnicog.discord.spanner.bot.command.InteractionContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

/**
 * @deprecated Use {@link NoActiveSessionEventV2} instead.
 */
@Deprecated
public class NoActiveSessionEvent extends AbstractCommandResult<InteractionContext> {

    public NoActiveSessionEvent(InteractionContext commandContext) {
        super(commandContext);
    }

}
