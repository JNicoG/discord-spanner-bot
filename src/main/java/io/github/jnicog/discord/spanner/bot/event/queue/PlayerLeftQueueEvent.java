package io.github.jnicog.discord.spanner.bot.event.queue;

import io.github.jnicog.discord.spanner.bot.command.CommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import io.github.jnicog.discord.spanner.bot.event.spanner.SpannerAwardingEvent;

import java.util.Set;

public class PlayerLeftQueueEvent extends AbstractCommandResult<CommandContext> implements SpannerAwardingEvent {

    private final Set<Long> updatedQueueSnapshot;
    private final int maxQueueSize;
    private final boolean isCheckInSessionActive;

    public PlayerLeftQueueEvent(CommandContext commandContext,
                                Set<Long> updatedQueue,
                                int maxQueueSize,
                                boolean isCheckInSessionActive) {
        super(commandContext);
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

    @Override
    public long getTargetUserId() {
        return getContext().userId();
    }

    @Override
    public long getTargetChannelId() {
        return getContext().channelId();
    }
}
