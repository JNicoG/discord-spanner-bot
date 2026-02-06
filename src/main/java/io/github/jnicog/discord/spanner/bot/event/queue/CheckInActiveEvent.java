package io.github.jnicog.discord.spanner.bot.event.queue;

import io.github.jnicog.discord.spanner.bot.command.ButtonContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

/**
 * @deprecated V1 event. Check-in during active session is now handled by UnkeenCommandHandlerV2.
 */
@Deprecated
public class CheckInActiveEvent extends AbstractCommandResult<ButtonContext> {

    public CheckInActiveEvent(ButtonContext commandContext) {
        super(commandContext);
    }

}
