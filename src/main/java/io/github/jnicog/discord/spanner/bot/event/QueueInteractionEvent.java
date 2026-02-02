package io.github.jnicog.discord.spanner.bot.event;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponder;
import io.github.jnicog.discord.spanner.bot.queue.QueueOutcome;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public class QueueInteractionEvent extends AbstractCommandResult {

    private final QueueOutcome queueOutcome;
    private final List<Long> queueSnapshot;
    private final int maxQueueSize;

    public QueueInteractionEvent(OffsetDateTime eventTime,
                                 String commandName,
                                 long userId,
                                 long channelId,
                                 String details,
                                 Map<String, Object> metadata,
                                 InteractionResponder interactionResponder,
                                 QueueOutcome queueOutcome,
                                 List<Long> queueSnapshot,
                                 int maxQueueSize) {
        super(eventTime, commandName, userId, channelId, details, metadata, interactionResponder);
        this.queueOutcome = queueOutcome;
        this.queueSnapshot = queueSnapshot;
        this.maxQueueSize = maxQueueSize;
    }

    public QueueOutcome getQueueOutcome() {
        return queueOutcome;
    }

    public List<Long> getQueueSnapshot() {
        return queueSnapshot;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }
}
