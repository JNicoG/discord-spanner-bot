package io.github.jnicog.discord.spanner.bot.queue;

import io.github.jnicog.discord.spanner.bot.config.QueueProperties;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Manages queues for different text channels.
 * A text channel (identified by textChannelId: Long) maps to a QueueChannel instance.
 */
@Service
public class ChannelQueueManager implements QueueService {

    private final QueueProperties queueProperties;
    private final ConcurrentMap<Long, ChannelQueue> channelQueues = new ConcurrentHashMap<>();

    public ChannelQueueManager(QueueProperties queueProperties) {
        this.queueProperties = queueProperties;
    }

    @Override
    public QueueOutcome joinQueue(long userId, long textChannelId) {
        ChannelQueue channelQueue = getOrCreateChannelQueue(textChannelId);
        return channelQueue.addUserToQueue(userId);
    }

    @Override
    public QueueOutcome leaveQueue(long userId, long textChannelId) {
        ChannelQueue channelQueue = getOrCreateChannelQueue(textChannelId);
        return channelQueue.removeUserFromQueue(userId);
    }

    @Override
    public Set<Long> showQueue(long textChannelId) {
        ChannelQueue channelQueue = getOrCreateChannelQueue(textChannelId);
        return channelQueue.snapshot();
    }

    @Override
    public int showMaxQueueSize(long textChannelId) {
        ChannelQueue channelQueue = getOrCreateChannelQueue(textChannelId);
        return channelQueue.getMaxQueueSize();
    }

    @Override
    public void clearQueue(long textChannelId) {
        ChannelQueue channelQueue = getOrCreateChannelQueue(textChannelId);
        channelQueue.clear();
    }

    private ChannelQueue getOrCreateChannelQueue(long textChannelId) {
        return channelQueues.computeIfAbsent(textChannelId, _ -> new ChannelQueue(queueProperties.getMaxQueueSize()));
    }
}
