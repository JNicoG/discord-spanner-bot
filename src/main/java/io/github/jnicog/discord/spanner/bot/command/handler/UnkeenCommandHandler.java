package io.github.jnicog.discord.spanner.bot.command.handler;

import io.github.jnicog.discord.spanner.bot.checkin.CancelResult;
import io.github.jnicog.discord.spanner.bot.checkin.CheckInAttemptResult;
import io.github.jnicog.discord.spanner.bot.checkin.CheckInService;
import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import io.github.jnicog.discord.spanner.bot.event.checkin.UnkeenDuringCheckInEvent;
import io.github.jnicog.discord.spanner.bot.event.queue.PlayerLeftQueueEvent;
import io.github.jnicog.discord.spanner.bot.event.queue.PlayerNotInQueueEvent;
import io.github.jnicog.discord.spanner.bot.queue.QueueOutcome;
import io.github.jnicog.discord.spanner.bot.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

/**
 *  handler for the /unkeen command.
 * Uses SlashCommandContext and returns  events.
 * If there's an active check-in session and the user is a participant,
 * the session is cancelled and remaining users stay in queue.
 */
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
    public AbstractCommandResult<?> handleCommand(SlashCommandContext context) {
        long userId = context.userId();
        long channelId = context.channelId();

        // Try to handle as check-in cancellation first
        Optional<AbstractCommandResult<?>> checkInCancellation = tryHandleCheckInCancellation(context, userId, channelId);
        if (checkInCancellation.isPresent()) {
            return checkInCancellation.get();
        }

        // Normal unkeen flow - no active session or user is not a participant
        return handleNormalUnkeen(context, userId, channelId);
    }

    private Optional<AbstractCommandResult<?>> tryHandleCheckInCancellation(
            SlashCommandContext context, long userId, long channelId) {

        if (!checkInService.hasActiveSession(channelId)) {
            return Optional.empty();
        }

        Set<Long> sessionParticipants = checkInService.getSessionParticipants(channelId);
        if (!sessionParticipants.contains(userId)) {
            return Optional.empty();
        }

        LOGGER.info("User {} is using /unkeen during active check-in in channel {}", userId, channelId);

        CancelResult cancelResult = checkInService.cancelAndGetRemainingUsers(channelId, userId);
        if (cancelResult.result() != CheckInAttemptResult.SESSION_CANCELLED) {
            return Optional.empty();
        }

        // Remove the cancelling user from the queue (remaining users stay)
        queueService.leaveQueue(userId, channelId);
        LOGGER.debug("Removed cancelling user {} from queue in channel {}", userId, channelId);

        Set<Long> queueSnapshot = queueService.showQueue(channelId);
        int maxQueueSize = queueService.showMaxQueueSize(channelId);

        return Optional.of(new UnkeenDuringCheckInEvent(
                context,
                queueSnapshot,
                maxQueueSize,
                cancelResult.messageId()
        ));
    }

    private AbstractCommandResult<?> handleNormalUnkeen(SlashCommandContext context, long userId, long channelId) {
        QueueOutcome outcome = queueService.leaveQueue(userId, channelId);
        Set<Long> queueSnapshot = queueService.showQueue(channelId);
        int maxQueueSize = queueService.showMaxQueueSize(channelId);

        return switch (outcome) {
            case DEQUEUED -> new PlayerLeftQueueEvent(context, queueSnapshot, maxQueueSize, false);
            case NOT_IN_QUEUE -> new PlayerNotInQueueEvent(context);
            default -> throw new IllegalStateException("Unexpected queue outcome: " + outcome);
        };
    }
}

