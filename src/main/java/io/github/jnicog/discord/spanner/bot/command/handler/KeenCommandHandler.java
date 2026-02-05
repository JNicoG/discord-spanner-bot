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
import java.util.List;
import java.util.Set;

@Component
public class KeenCommandHandler implements SlashCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeenCommandHandler.class);

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
    public boolean isEphemeral() {
        return false;
    }

    @Override
    public AbstractCommandResult handleCommand(CommandContext context) {
        OffsetDateTime eventTime = context.eventTime();
        long userId = context.userId();
        long channelId = context.channelId();

        // These two should be joined into one atomic operation
        QueueOutcome outcome = queueService.joinQueue(userId, channelId);
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
