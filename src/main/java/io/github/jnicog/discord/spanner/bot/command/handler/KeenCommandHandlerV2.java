package io.github.jnicog.discord.spanner.bot.command.handler;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResultV2;
import io.github.jnicog.discord.spanner.bot.event.queue.PlayerAlreadyQueuedEventV2;
import io.github.jnicog.discord.spanner.bot.event.queue.PlayerJoinedQueueEventV2;
import io.github.jnicog.discord.spanner.bot.event.queue.QueueAlreadyFullEventV2;
import io.github.jnicog.discord.spanner.bot.queue.QueueOutcome;
import io.github.jnicog.discord.spanner.bot.queue.QueueService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * V2 handler for the /keen command.
 * Uses SlashCommandContext and returns V2 events.
 */
@Component
public class KeenCommandHandlerV2 implements SlashCommandHandlerV2 {


    private final QueueService queueService;

    public KeenCommandHandlerV2(QueueService queueService) {
        this.queueService = queueService;
    }

    @Override
    public String getCommandName() {
        return "keen";
    }

    @Override
    public List<String> getAliases() {
        return List.of("k");
    }

    @Override
    public AbstractCommandResultV2<?> handleCommand(SlashCommandContext context) {
        long userId = context.userId();
        long channelId = context.channelId();

        // These two should be joined into one atomic operation
        QueueOutcome outcome = queueService.joinQueue(userId, channelId);
        Set<Long> queueSnapshot = queueService.showQueue(channelId);

        int maxQueueSize = queueService.showMaxQueueSize(channelId);

        return switch (outcome) {
            case ENQUEUED -> new PlayerJoinedQueueEventV2(context, queueSnapshot, maxQueueSize);
            case QUEUE_FULL -> new QueueAlreadyFullEventV2(context);
            case ALREADY_QUEUED -> new PlayerAlreadyQueuedEventV2(context);
            default -> throw new IllegalStateException("Unexpected queue outcome: " + outcome);
        };
    }
}

