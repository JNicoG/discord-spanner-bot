package io.github.jnicog.discord.spanner.bot.event.checkin;

import io.github.jnicog.discord.spanner.bot.command.ButtonInteractionContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

/**
 *  event for when the check-in session has expired.
 */
public class ExpiredSessionCheckInEvent extends AbstractCommandResult<ButtonInteractionContext> {

    public ExpiredSessionCheckInEvent(ButtonInteractionContext context) {
        super(context);
    }
}

