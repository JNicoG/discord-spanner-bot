package io.github.jnicog.discord.spanner.bot.event.checkin;

import io.github.jnicog.discord.spanner.bot.command.ButtonInteractionContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResultV2;

import java.util.Map;

/**
 * V2 event for when a player successfully checks in.
 */
public class PlayerCheckInEventV2 extends AbstractCommandResultV2<ButtonInteractionContext> implements CheckInEvent {

    private final long checkInMessageId;
    private final Map<Long, Boolean> updatedCheckInSnapshot;

    public PlayerCheckInEventV2(ButtonInteractionContext context,
                                Map<Long, Boolean> updatedCheckInSnapshot,
                                long checkInMessageId) {
        super(context);
        this.updatedCheckInSnapshot = Map.copyOf(updatedCheckInSnapshot);
        this.checkInMessageId = checkInMessageId;
    }

    @Override
    public long getCheckInMessageId() {
        return checkInMessageId;
    }

    public Map<Long, Boolean> getUpdatedCheckInSnapshot() {
        return updatedCheckInSnapshot;
    }
}

