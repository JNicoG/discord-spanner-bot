package io.github.jnicog.discord.spanner.bot.event.checkin;

import io.github.jnicog.discord.spanner.bot.command.ButtonInteractionContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResultV2;

/**
 * V2 event for when a player tries to check in without authorisation.
 */
public class UnauthorisedCheckInEventV2 extends AbstractCommandResultV2<ButtonInteractionContext> {

    public UnauthorisedCheckInEventV2(ButtonInteractionContext context) {
        super(context);
    }
}

