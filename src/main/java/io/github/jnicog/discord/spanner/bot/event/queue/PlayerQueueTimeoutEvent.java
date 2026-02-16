package io.github.jnicog.discord.spanner.bot.event.queue;

import java.util.Set;

/**
 * Event published when a player is removed from the queue due to inactivity timeout.
 *
 * <p>This event does NOT implement SpannerAwardingEvent because queue inactivity
 * timeouts should not penalize users with a spanner.</p>
 *
 * <p>This is a non-command event (not triggered by user interaction), so it doesn't
 * extend AbstractCommandResult.</p>
 */
public class PlayerQueueTimeoutEvent {

    private final long userId;
    private final long channelId;
    private final Set<Long> remainingQueueSnapshot;
    private final int maxQueueSize;

    public PlayerQueueTimeoutEvent(long userId,
                                   long channelId,
                                   Set<Long> remainingQueueSnapshot,
                                   int maxQueueSize) {
        this.userId = userId;
        this.channelId = channelId;
        this.remainingQueueSnapshot = Set.copyOf(remainingQueueSnapshot);
        this.maxQueueSize = maxQueueSize;
    }

    public long getUserId() {
        return userId;
    }

    public long getChannelId() {
        return channelId;
    }

    public Set<Long> getRemainingQueueSnapshot() {
        return remainingQueueSnapshot;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }
}

