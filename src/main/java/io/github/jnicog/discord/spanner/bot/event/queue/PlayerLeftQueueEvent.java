package io.github.jnicog.discord.spanner.bot.event.queue;

import io.github.jnicog.discord.spanner.bot.command.CommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import io.github.jnicog.discord.spanner.bot.event.spanner.SpannerAwardingEvent;

import java.util.Set;

public class PlayerLeftQueueEvent extends AbstractCommandResult implements SpannerAwardingEvent {

    private final Set<Long> updatedQueueSnapshot;
    private final int maxQueueSize;

    public PlayerLeftQueueEvent(CommandContext commandContext,
                                Set<Long> updatedQueue,
                                int maxQueueSize) {
        super(commandContext);
        this.updatedQueueSnapshot = Set.copyOf(updatedQueue);
        this.maxQueueSize = maxQueueSize;
    }

    public Set<Long> getUpdatedQueueSnapshot() {
        return updatedQueueSnapshot;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    @Override
    public long getTargetUserId() {
        return context.userId();
    }

    @Override
    public long getTargetChannelId() {
        return context.channelId();
    }
}
