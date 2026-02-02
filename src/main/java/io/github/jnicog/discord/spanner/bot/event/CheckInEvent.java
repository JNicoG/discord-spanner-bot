package io.github.jnicog.discord.spanner.bot.event;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponder;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public class CheckInEvent extends AbstractCommandResult {

    private final List<Long> queueSnapshot;
    private final int maxQueueSize;

    public CheckInEvent(OffsetDateTime eventTime,
                        String commandName,
                        long userId,
                        long channelId,
                        String details,
                        Map<String, Object> metadata,
                        InteractionResponder interactionResponder,
                        List<Long> queueSnapshot,
                        int maxQueueSize) {
        super(eventTime, commandName, userId, channelId, details, metadata, interactionResponder);
        this.queueSnapshot = queueSnapshot;
        this.maxQueueSize = maxQueueSize;
    }
}
