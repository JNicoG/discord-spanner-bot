package io.github.jnicog.discord.spanner.bot.event.tenman;

import io.github.jnicog.discord.spanner.bot.command.ButtonInteractionContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

/** Fired when a button is clicked on a locked/closed poll and no state change is possible. */
public class TenManLockedPollInteractionEvent extends AbstractCommandResult<ButtonInteractionContext> {

    private final String message;

    public TenManLockedPollInteractionEvent(ButtonInteractionContext context, String message) {
        super(context);
        this.message = message;
    }

    public String getMessage() { return message; }
}
