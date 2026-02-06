package io.github.jnicog.discord.spanner.bot.event.queue;

import io.github.jnicog.discord.spanner.bot.command.CommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

import java.util.Set;

public class QueueQueryEvent extends AbstractCommandResult<CommandContext> {

    Set<Long> currentQueueSnapshot;
    int maxQueueSize;

    public QueueQueryEvent(CommandContext commandContext,
                           Set<Long> currentQueueSnapshot,
                           int maxQueueSize) {
        super(commandContext);
        this.currentQueueSnapshot = Set.copyOf(currentQueueSnapshot);
        this.maxQueueSize = maxQueueSize;
    }

    public Set<Long> getCurrentQueueSnapshot() {
        return currentQueueSnapshot;
    }
}
