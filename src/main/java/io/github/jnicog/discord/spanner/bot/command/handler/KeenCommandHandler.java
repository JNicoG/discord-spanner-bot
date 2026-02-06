package io.github.jnicog.discord.spanner.bot.command.handler;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import io.github.jnicog.discord.spanner.bot.event.queue.PlayerAlreadyQueuedEvent;
import io.github.jnicog.discord.spanner.bot.event.queue.PlayerJoinedQueueEvent;
import io.github.jnicog.discord.spanner.bot.event.queue.QueueAlreadyFullEvent;
import io.github.jnicog.discord.spanner.bot.queue.QueueOutcome;
import io.github.jnicog.discord.spanner.bot.queue.QueueService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 *  handler for the /keen command.
 * Uses SlashCommandContext and returns  events.
 */
@Component
public class KeenCommandHandler implements SlashCommandHandler {


    private final QueueService queueService;

    public KeenCommandHandler(QueueService queueService) {
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
    public AbstractCommandResult<?> handleCommand(SlashCommandContext context) {
        long userId = context.userId();
        long channelId = context.channelId();

        // These two should be joined into one atomic operation
        QueueOutcome outcome = queueService.joinQueue(userId, channelId);
        Set<Long> queueSnapshot = queueService.showQueue(channelId);

        int maxQueueSize = queueService.showMaxQueueSize(channelId);

        return switch (outcome) {
            case ENQUEUED -> new PlayerJoinedQueueEvent(context, queueSnapshot, maxQueueSize);
            case QUEUE_FULL -> new QueueAlreadyFullEvent(context);
            case ALREADY_QUEUED -> new PlayerAlreadyQueuedEvent(context);
            default -> throw new IllegalStateException("Unexpected queue outcome: " + outcome);
        };
    }
}

