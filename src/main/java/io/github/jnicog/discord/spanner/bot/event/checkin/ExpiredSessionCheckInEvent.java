package io.github.jnicog.discord.spanner.bot.event.checkin;

import io.github.jnicog.discord.spanner.bot.command.InteractionContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

/**
 * @deprecated Use {@link ExpiredSessionCheckInEventV2} instead.
 */
@Deprecated
public class ExpiredSessionCheckInEvent extends AbstractCommandResult<InteractionContext>  {

    public ExpiredSessionCheckInEvent(InteractionContext context) {
        super(context);
    }

}
