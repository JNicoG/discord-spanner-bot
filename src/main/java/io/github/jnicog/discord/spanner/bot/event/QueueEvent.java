package io.github.jnicog.discord.spanner.bot.event;

import io.github.jnicog.discord.spanner.bot.model.ChannelQueue;

public abstract class QueueEvent {
    private final ChannelQueue queue;

    public QueueEvent(ChannelQueue queue) {
        this.queue = queue;
    }

    public ChannelQueue getQueue() {
        return queue;
    }

    public long getChannelId() {
        return queue.getChannelId();
    }
}
