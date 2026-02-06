package io.github.jnicog.discord.spanner.bot.event.checkin;

import io.github.jnicog.discord.spanner.bot.command.ButtonContext;
import io.github.jnicog.discord.spanner.bot.command.InteractionContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

/**
 * @deprecated Use {@link UnauthorisedCheckInEventV2} instead.
 */
@Deprecated
public class UnauthorisedCheckInEvent extends AbstractCommandResult<InteractionContext> {

    public UnauthorisedCheckInEvent(InteractionContext commandContext) {
        super(commandContext);
    }

}
