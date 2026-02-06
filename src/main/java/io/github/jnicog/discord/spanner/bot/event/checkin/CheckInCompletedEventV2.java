package io.github.jnicog.discord.spanner.bot.event.checkin;

import io.github.jnicog.discord.spanner.bot.command.ButtonInteractionContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResultV2;

import java.util.Map;

/**
 * V2 event for when all players have completed check-in.
 * Uses JDA-free ButtonInteractionContext.
 */
public class CheckInCompletedEventV2 extends AbstractCommandResultV2<ButtonInteractionContext> {

    private final long checkInMessageId;
    private final Map<Long, Boolean> finalCheckInSnapshot;

    public CheckInCompletedEventV2(ButtonInteractionContext context,
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

