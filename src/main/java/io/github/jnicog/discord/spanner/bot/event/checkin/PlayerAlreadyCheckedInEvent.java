package io.github.jnicog.discord.spanner.bot.event.checkin;

import io.github.jnicog.discord.spanner.bot.command.ButtonInteractionContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

/**
 * Event for when a player is already checked in.
 */
public class PlayerAlreadyCheckedInEvent extends AbstractCommandResult<ButtonInteractionContext> {

    public PlayerAlreadyCheckedInEvent(ButtonInteractionContext context) {
        super(context);
    }
}

