package io.github.jnicog.discord.spanner.bot.event.checkin;

import io.github.jnicog.discord.spanner.bot.command.ButtonInteractionContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResultV2;

/**
 * V2 event for when the check-in session has expired.
 */
public class ExpiredSessionCheckInEventV2 extends AbstractCommandResultV2<ButtonInteractionContext> {

    public ExpiredSessionCheckInEventV2(ButtonInteractionContext context) {
        super(context);
    }
}

