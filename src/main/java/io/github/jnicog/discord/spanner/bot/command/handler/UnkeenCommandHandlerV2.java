package io.github.jnicog.discord.spanner.bot.command.handler;

import io.github.jnicog.discord.spanner.bot.checkin.CheckInService;
import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResultV2;
import io.github.jnicog.discord.spanner.bot.event.queue.PlayerLeftQueueEventV2;
import io.github.jnicog.discord.spanner.bot.event.queue.PlayerNotInQueueEventV2;
import io.github.jnicog.discord.spanner.bot.queue.QueueOutcome;
import io.github.jnicog.discord.spanner.bot.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * V2 handler for the /unkeen command.
 * Uses SlashCommandContext and returns V2 events.
 */
@Component
public class UnkeenCommandHandlerV2 implements SlashCommandHandlerV2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnkeenCommandHandlerV2.class);

    private final QueueService queueService;
    private final CheckInService checkInService;

    public UnkeenCommandHandlerV2(QueueService queueService, CheckInService checkInService) {
        this.queueService = queueService;
        this.checkInService = checkInService;
    }

    @Override
    public String getCommandName() {
        return "unkeen";
    }

    @Override
    public AbstractCommandResultV2<?> handleCommand(SlashCommandContext context) {
        long userId = context.userId();
        long channelId = context.channelId();
        boolean hasActiveSession = checkInService.hasActiveSession(channelId);

        // These two should be joined into one atomic operation
        QueueOutcome outcome = queueService.leaveQueue(userId, channelId);
        Set<Long> queueSnapshot = queueService.showQueue(channelId);

        int maxQueueSize = queueService.showMaxQueueSize(channelId);

        return switch (outcome) {
            case DEQUEUED -> new PlayerLeftQueueEventV2(context, queueSnapshot, maxQueueSize, hasActiveSession);
            case NOT_IN_QUEUE -> new PlayerNotInQueueEventV2(context);
            default -> throw new IllegalStateException("Unexpected queue outcome: " + outcome);
        };
    }
}

