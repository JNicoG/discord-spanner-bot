package io.github.jnicog.discord.spanner.bot.queue;

import java.util.List;

public interface QueueService {

    QueueOutcome joinQueue(long userId, long textChannelId);

    QueueOutcome leaveQueue(long userId, long textChannelId);

    List<Long> showQueue(long textChannelId);

    int showMaxQueueSize(long textChannelId);

}
