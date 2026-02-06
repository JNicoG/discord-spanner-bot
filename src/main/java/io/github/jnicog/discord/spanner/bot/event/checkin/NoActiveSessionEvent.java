package io.github.jnicog.discord.spanner.bot.event.checkin;

import io.github.jnicog.discord.spanner.bot.command.ButtonInteractionContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

/**
 *  event for when there is no active check-in session.
 */
public class NoActiveSessionEvent extends AbstractCommandResult<ButtonInteractionContext> {

    public NoActiveSessionEvent(ButtonInteractionContext context) {
        super(context);
    }
}

