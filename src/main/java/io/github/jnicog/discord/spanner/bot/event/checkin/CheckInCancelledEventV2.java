package io.github.jnicog.discord.spanner.bot.event.checkin;

import io.github.jnicog.discord.spanner.bot.command.ButtonInteractionContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResultV2;
import io.github.jnicog.discord.spanner.bot.event.spanner.SpannerAwardingEvent;

import java.util.Set;

/**
 * V2 event for when a player cancels/spanners the check-in session.
 * Uses JDA-free ButtonInteractionContext.
 */
public class CheckInCancelledEventV2 extends AbstractCommandResultV2<ButtonInteractionContext> implements SpannerAwardingEvent {

    private final long checkInMessageId;
    private final Set<Long> remainingUsersForQueue;
    private final int maxQueueSize;

    public CheckInCancelledEventV2(ButtonInteractionContext context,
                                   long checkInMessageId,
                                   Set<Long> remainingUsersForQueue,
                                   int maxQueueSize) {
        super(context);
        this.checkInMessageId = checkInMessageId;
        this.remainingUsersForQueue = Set.copyOf(remainingUsersForQueue);
        this.maxQueueSize = maxQueueSize;
    }

    public long getCheckInMessageId() {
        return checkInMessageId;
    }

    /**
     * Returns the user IDs that should be added back to the queue.
     * This excludes the user who cancelled the session.
     */
    public Set<Long> getRemainingUsersForQueue() {
        return remainingUsersForQueue;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    @Override
    public long getTargetUserId() {
        return getContext().userId();
    }

    @Override
    public long getTargetChannelId() {
        return getContext().channelId();
    }
}

