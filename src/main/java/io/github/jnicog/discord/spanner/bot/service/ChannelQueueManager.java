package io.github.jnicog.discord.spanner.bot.service;

import io.github.jnicog.discord.spanner.bot.config.QueueProperties;
import io.github.jnicog.discord.spanner.bot.model.ChannelQueue;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChannelQueueManager {
    public static Logger LOGGER = LoggerFactory.getLogger(ChannelQueueManager.class);

    private final Map<MessageChannel, ChannelQueue> channelQueues = new ConcurrentHashMap<>();
    private final QueueProperties queueProperties;
    private final SpannerService spannerService;
    private final QueueEventPublisher eventPublisher;

    public ChannelQueueManager(QueueProperties queueProperties, SpannerService spannerService, QueueEventPublisher eventPublisher) {
        this.queueProperties = queueProperties;
        this.spannerService = spannerService;
        this.eventPublisher = eventPublisher;
    }

    public synchronized ChannelQueue getOrCreateQueue(MessageChannel channel) {
        return channelQueues.computeIfAbsent(channel, c -> {
            LOGGER.info("Creating new queue for channel {}", channel.getIdLong());
            return new ChannelQueue(channel, queueProperties, spannerService, eventPublisher);
        });
    }

    public void removeIfEmpty(MessageChannel channel) {
        channelQueues.computeIfPresent(channel, (c, queue) -> {
            if (queue.isEmpty()) {
                LOGGER.info("Removing empty queue for channel {}", channel.getIdLong());
                queue.shutdown();
                return null;
            }
            return queue;
        });
    }

    @Scheduled(fixedRate = 15 * 60 * 1000) // Run every 15 minutes
    public void cleanupInactiveQueues() {
        LOGGER.info("Running scheduled cleanup of inactive queues");
        channelQueues.entrySet().removeIf(entry -> {
            ChannelQueue queue = entry.getValue();
            if (queue.isEmpty()) {
                LOGGER.info("Cleaning up empty queue for channel {}", entry.getKey());
                queue.shutdown();
                return true;
            }

            // Also remove queues that have been inactive for a long time
            if (Duration.between(queue.getLastActivityTime(), Instant.now())
                    .toMinutes() > queueProperties.getQueueInactivityTimeoutLength()) {
                LOGGER.info("Cleaning up inactive queue for channel {}", entry.getKey());
                queue.shutdown();
                return true;
            }

            return false;
        });
    }

    public void shutdown() {
        LOGGER.info("Shutting down ChannelQueueManager, cleaning up {} queues", channelQueues.size());
        channelQueues.values().forEach(ChannelQueue::shutdown);
        channelQueues.clear();
    }

    public void removeQueueIfEmpty(MessageChannel channel) {
    }
}
