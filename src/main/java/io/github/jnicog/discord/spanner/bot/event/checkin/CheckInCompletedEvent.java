package io.github.jnicog.discord.spanner.bot.event.checkin;

import io.github.jnicog.discord.spanner.bot.command.ButtonInteractionContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

import java.util.Map;

/**
 * Event for when all players have completed check-in.
 * Uses JDA-free ButtonInteractionContext.
 */
public class CheckInCompletedEvent extends AbstractCommandResult<ButtonInteractionContext> {

    private final long checkInMessageId;
    private final Map<Long, Boolean> finalCheckInSnapshot;

    public CheckInCompletedEvent(ButtonInteractionContext context,
                                   Map<Long, Boolean> finalCheckInSnapshot,
                                   long checkInMessageId) {
        super(context);
        this.finalCheckInSnapshot = Map.copyOf(finalCheckInSnapshot);
        this.checkInMessageId = checkInMessageId;
    }

    public long getCheckInMessageId() {
        return checkInMessageId;
    }

    public Map<Long, Boolean> getFinalCheckInSnapshot() {
        return finalCheckInSnapshot;
    }
}

