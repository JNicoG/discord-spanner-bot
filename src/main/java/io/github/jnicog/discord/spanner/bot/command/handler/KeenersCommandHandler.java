package io.github.jnicog.discord.spanner.bot.command.handler;

import io.github.jnicog.discord.spanner.bot.command.CommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import io.github.jnicog.discord.spanner.bot.event.QueueInteractionEvent;
import io.github.jnicog.discord.spanner.bot.queue.QueueOutcome;
import io.github.jnicog.discord.spanner.bot.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Set;

@Component
public class KeenersCommandHandler implements SlashCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeenersCommandHandler.class);

    private final QueueService queueService;

    public KeenersCommandHandler(QueueService queueService) {
        this.queueService = queueService;
    }

    @Override
    public String getCommandName() {
        return "keeners";
    }

    @Override
    public boolean isEphemeral() {
        return false;
    }

    @Override
    public AbstractCommandResult handleCommand(CommandContext context) {
        OffsetDateTime eventTime = context.eventTime();
        long userId = context.userId();
        long channelId = context.channelId();

        Set<Long> queueSnapshot = queueService.showQueue(channelId);

        int maxQueueSize = queueService.showMaxQueueSize(channelId);

        return new QueueInteractionEvent(
                eventTime,
                getCommandName(),
                userId,
                channelId,
                null,
                null,
                context.interactionResponder(),
                QueueOutcome.QUERY,
                queueSnapshot,
                maxQueueSize
        );
    }
}
