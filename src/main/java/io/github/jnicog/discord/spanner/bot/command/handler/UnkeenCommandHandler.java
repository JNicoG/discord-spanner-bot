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
public class UnkeenCommandHandler implements SlashCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnkeenCommandHandler.class);

    private final QueueService queueService;

    public UnkeenCommandHandler(QueueService queueService) {
        this.queueService = queueService;
    }

    @Override
    public String getCommandName() {
        return "unkeen";
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

        QueueOutcome outcome = queueService.leaveQueue(userId, channelId);
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
                outcome,
                queueSnapshot,
                maxQueueSize
        );
    }
}
