package io.github.jnicog.discord.spanner.bot.event.checkin;

import io.github.jnicog.discord.spanner.bot.command.ButtonInteractionContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

/**
 * Event for when a player tries to check in without authorisation.
 */
public class UnauthorisedCheckInEvent extends AbstractCommandResult<ButtonInteractionContext> {

    public UnauthorisedCheckInEvent(ButtonInteractionContext context) {
        super(context);
    }
}

