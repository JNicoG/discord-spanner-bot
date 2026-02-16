package io.github.jnicog.discord.spanner.bot.event.queue;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

import java.util.Set;

/**
 * Event for querying the current queue state.
 */
public class QueueQueryEvent extends AbstractCommandResult<SlashCommandContext> {

    private final Set<Long> currentQueueSnapshot;
    private final int maxQueueSize;

    public QueueQueryEvent(SlashCommandContext context,
                             Set<Long> currentQueueSnapshot,
                             int maxQueueSize) {
        super(context);
        this.currentQueueSnapshot = Set.copyOf(currentQueueSnapshot);
        this.maxQueueSize = maxQueueSize;
    }

    public Set<Long> getCurrentQueueSnapshot() {
        return currentQueueSnapshot;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }
}

