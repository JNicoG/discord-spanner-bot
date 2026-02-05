package io.github.jnicog.discord.spanner.bot.event.queue;

import io.github.jnicog.discord.spanner.bot.command.CommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

import java.util.Set;

public class PlayerLeftQueueEvent extends AbstractCommandResult {

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

}
