package io.github.jnicog.discord.spanner.bot.command.handler;

import io.github.jnicog.discord.spanner.bot.checkin.CheckInService;
import io.github.jnicog.discord.spanner.bot.command.CommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import io.github.jnicog.discord.spanner.bot.event.queue.PlayerLeftQueueEvent;
import io.github.jnicog.discord.spanner.bot.event.queue.PlayerNotInQueueEvent;
import io.github.jnicog.discord.spanner.bot.queue.QueueOutcome;
import io.github.jnicog.discord.spanner.bot.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class UnkeenCommandHandler implements SlashCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnkeenCommandHandler.class);

    private final QueueService queueService;
    private final CheckInService checkInService;

    public UnkeenCommandHandler(QueueService queueService, CheckInService checkInService) {
        this.queueService = queueService;
        this.checkInService = checkInService;
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
    public AbstractCommandResult<?> handleCommand(CommandContext context) {
        long userId = context.userId();
        long channelId = context.channelId();
        boolean hasActiveSession = checkInService.hasActiveSession(channelId);

        // These two should be joined into one atomic operation
        QueueOutcome outcome = queueService.leaveQueue(userId, channelId);
        Set<Long> queueSnapshot = queueService.showQueue(channelId);

        int maxQueueSize = queueService.showMaxQueueSize(channelId);

        return switch (outcome) {
            case DEQUEUED -> new PlayerLeftQueueEvent(context, queueSnapshot, maxQueueSize, hasActiveSession);
            case NOT_IN_QUEUE -> new PlayerNotInQueueEvent(context);
            default -> throw new IllegalStateException("Unexpected queue outcome: " + outcome);
        };
    }
}
