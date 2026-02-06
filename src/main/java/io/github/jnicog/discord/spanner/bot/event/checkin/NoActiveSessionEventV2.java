package io.github.jnicog.discord.spanner.bot.event.checkin;

import io.github.jnicog.discord.spanner.bot.command.ButtonInteractionContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResultV2;

/**
 * V2 event for when there is no active check-in session.
 */
public class NoActiveSessionEventV2 extends AbstractCommandResultV2<ButtonInteractionContext> {

    public NoActiveSessionEventV2(ButtonInteractionContext context) {
        super(context);
    }
}

