package io.github.jnicog.discord.spanner.bot.event.queue;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResultV2;

import java.util.Set;

/**
 * V2 event for when a player successfully joins the queue.
 */
public class PlayerJoinedQueueEventV2 extends AbstractCommandResultV2<SlashCommandContext> {

    private final Set<Long> updatedQueueSnapshot;
    private final int maxQueueSize;

    public PlayerJoinedQueueEventV2(SlashCommandContext context,
                                    Set<Long> updatedQueue,
                                    int maxQueueSize) {
        super(context);
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

