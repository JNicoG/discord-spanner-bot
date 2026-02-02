package io.github.jnicog.discord.spanner.bot.queue;

import io.github.jnicog.discord.spanner.bot.config.QueueProperties;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
    public List<Long> showQueue(long textChannelId) {
        ChannelQueue channelQueue = getOrCreateChannelQueue(textChannelId);
        return channelQueue.snapshot();
    }

    @Override
    public int showMaxQueueSize(long textChannelId) {
        ChannelQueue channelQueue = getOrCreateChannelQueue(textChannelId);
        return channelQueue.getMaxQueueSize();
    }

    private ChannelQueue getOrCreateChannelQueue(long textChannelId) {
        return channelQueues.computeIfAbsent(textChannelId, _ -> new ChannelQueue(queueProperties.getMaxQueueSize()));
    }
}
