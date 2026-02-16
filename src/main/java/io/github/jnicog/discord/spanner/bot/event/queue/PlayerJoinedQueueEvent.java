package io.github.jnicog.discord.spanner.bot.event.queue;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

import java.util.Set;

/**
 * Event for when a player successfully joins the queue.
 */
public class PlayerJoinedQueueEvent extends AbstractCommandResult<SlashCommandContext> {

    private final Set<Long> updatedQueueSnapshot;
    private final int maxQueueSize;

    public PlayerJoinedQueueEvent(SlashCommandContext context,
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

