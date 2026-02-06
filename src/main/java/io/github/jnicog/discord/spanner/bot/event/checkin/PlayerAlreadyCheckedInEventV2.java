package io.github.jnicog.discord.spanner.bot.event.checkin;

import io.github.jnicog.discord.spanner.bot.command.ButtonInteractionContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResultV2;

/**
 * V2 event for when a player is already checked in.
 */
public class PlayerAlreadyCheckedInEventV2 extends AbstractCommandResultV2<ButtonInteractionContext> {

    public PlayerAlreadyCheckedInEventV2(ButtonInteractionContext context) {
        super(context);
    }
}

