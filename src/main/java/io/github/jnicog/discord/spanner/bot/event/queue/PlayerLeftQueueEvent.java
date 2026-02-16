package io.github.jnicog.discord.spanner.bot.event.queue;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import io.github.jnicog.discord.spanner.bot.event.spanner.SpannerAwardingEvent;

import java.util.Set;

/**
 * Event for when a player leaves the queue.
 */
public class PlayerLeftQueueEvent extends AbstractCommandResult<SlashCommandContext> implements SpannerAwardingEvent {

    private final Set<Long> updatedQueueSnapshot;
    private final int maxQueueSize;
    private final boolean isCheckInSessionActive;

    public PlayerLeftQueueEvent(SlashCommandContext context,
                                  Set<Long> updatedQueue,
                                  int maxQueueSize,
                                  boolean isCheckInSessionActive) {
        super(context);
        this.updatedQueueSnapshot = Set.copyOf(updatedQueue);
        this.maxQueueSize = maxQueueSize;
        this.isCheckInSessionActive = isCheckInSessionActive;
    }

    public Set<Long> getUpdatedQueueSnapshot() {
        return updatedQueueSnapshot;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    public boolean isCheckInSessionActive() {
        return isCheckInSessionActive;
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

