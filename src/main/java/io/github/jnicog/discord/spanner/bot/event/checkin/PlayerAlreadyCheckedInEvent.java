package io.github.jnicog.discord.spanner.bot.event.checkin;

import io.github.jnicog.discord.spanner.bot.command.ButtonContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

/**
 * @deprecated Use {@link PlayerAlreadyCheckedInEventV2} instead.
 */
@Deprecated
public class PlayerAlreadyCheckedInEvent extends AbstractCommandResult<ButtonContext> {

    public PlayerAlreadyCheckedInEvent(ButtonContext context) {
        super(context);
    }

}
