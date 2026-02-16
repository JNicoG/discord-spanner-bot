package io.github.jnicog.discord.spanner.bot.command.handler;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import io.github.jnicog.discord.spanner.bot.event.queue.QueueQueryEvent;
import io.github.jnicog.discord.spanner.bot.queue.QueueService;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Handler for the /keeners command.
 * Uses SlashCommandContext.
 */
@Component
public class KeenersCommandHandler implements SlashCommandHandler {


    private final QueueService queueService;

    public KeenersCommandHandler(QueueService queueService) {
        this.queueService = queueService;
    }

    @Override
    public String getCommandName() {
        return "keeners";
    }

    @Override
    public AbstractCommandResult<?> handleCommand(SlashCommandContext context) {
        long channelId = context.channelId();

        Set<Long> queueSnapshot = queueService.showQueue(channelId);
        int maxQueueSize = queueService.showMaxQueueSize(channelId);

        return new QueueQueryEvent(context, queueSnapshot, maxQueueSize);
    }
}

