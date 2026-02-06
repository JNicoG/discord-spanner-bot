package io.github.jnicog.discord.spanner.bot.event.checkin;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import io.github.jnicog.discord.spanner.bot.event.spanner.SpannerAwardingEvent;

import java.util.Set;

/**
 *  event for when a player uses /unkeen during an active check-in session.
 * This cancels the session and the user receives a spanner.
 */
public class UnkeenDuringCheckInEvent extends AbstractCommandResult<SlashCommandContext> implements SpannerAwardingEvent {

    private final Set<Long> remainingUsersInQueue;
    private final int maxQueueSize;
    private final long checkInMessageId;

    public UnkeenDuringCheckInEvent(SlashCommandContext context,
                                      Set<Long> remainingUsersInQueue,
                                      int maxQueueSize,
                                      long checkInMessageId) {
        super(context);
        this.remainingUsersInQueue = Set.copyOf(remainingUsersInQueue);
        this.maxQueueSize = maxQueueSize;
        this.checkInMessageId = checkInMessageId;
    }

    public Set<Long> getRemainingUsersInQueue() {
        return remainingUsersInQueue;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    public long getCheckInMessageId() {
        return checkInMessageId;
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

