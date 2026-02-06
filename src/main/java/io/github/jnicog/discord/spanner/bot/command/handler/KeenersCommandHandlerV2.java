package io.github.jnicog.discord.spanner.bot.command.handler;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResultV2;
import io.github.jnicog.discord.spanner.bot.event.queue.QueueQueryEventV2;
import io.github.jnicog.discord.spanner.bot.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * V2 handler for the /keeners command.
 * Uses SlashCommandContext and returns V2 events.
 */
@Component
public class KeenersCommandHandlerV2 implements SlashCommandHandlerV2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeenersCommandHandlerV2.class);

    private final QueueService queueService;

    public KeenersCommandHandlerV2(QueueService queueService) {
        this.queueService = queueService;
    }

    @Override
    public String getCommandName() {
        return "keeners";
    }

    @Override
    public AbstractCommandResultV2<?> handleCommand(SlashCommandContext context) {
        long channelId = context.channelId();

        Set<Long> queueSnapshot = queueService.showQueue(channelId);
        int maxQueueSize = queueService.showMaxQueueSize(channelId);

        return new QueueQueryEventV2(context, queueSnapshot, maxQueueSize);
    }
}

