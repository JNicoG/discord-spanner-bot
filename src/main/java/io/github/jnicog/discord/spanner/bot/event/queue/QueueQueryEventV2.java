package io.github.jnicog.discord.spanner.bot.event.queue;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResultV2;

import java.util.Set;

/**
 * V2 event for querying the current queue state.
 */
public class QueueQueryEventV2 extends AbstractCommandResultV2<SlashCommandContext> {

    private final Set<Long> currentQueueSnapshot;
    private final int maxQueueSize;

    public QueueQueryEventV2(SlashCommandContext context,
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

