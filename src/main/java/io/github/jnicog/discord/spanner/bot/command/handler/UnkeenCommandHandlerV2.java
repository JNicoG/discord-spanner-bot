package io.github.jnicog.discord.spanner.bot.command.handler;

import io.github.jnicog.discord.spanner.bot.checkin.CancelResult;
import io.github.jnicog.discord.spanner.bot.checkin.CheckInService;
import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResultV2;
import io.github.jnicog.discord.spanner.bot.event.checkin.UnkeenDuringCheckInEventV2;
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
 * If there's an active check-in session and the user is a participant,
 * the session is cancelled and remaining users are re-queued.
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

        // Check if there's an active check-in session and user is a participant
        if (checkInService.hasActiveSession(channelId)) {
            Set<Long> sessionParticipants = checkInService.getSessionParticipants(channelId);

            if (sessionParticipants.contains(userId)) {
                LOGGER.info("User {} is using /unkeen during active check-in in channel {}", userId, channelId);

                // Cancel the session and get remaining users
                CancelResult cancelResult = checkInService.cancelAndGetRemainingUsers(channelId, userId);

                if (cancelResult.result() == io.github.jnicog.discord.spanner.bot.checkin.CheckInAttemptResult.SESSION_CANCELLED) {
                    // Remove the cancelling user from the queue
                    // (remaining users stay in the queue - they were never removed)
                    queueService.leaveQueue(userId, channelId);
                    LOGGER.debug("Removed cancelling user {} from queue in channel {}", userId, channelId);

                    // Get the updated queue snapshot (should now exclude the cancelling user)
                    Set<Long> queueSnapshot = queueService.showQueue(channelId);
                    int maxQueueSize = queueService.showMaxQueueSize(channelId);

                    return new UnkeenDuringCheckInEventV2(
                            context,
                            queueSnapshot,
                            maxQueueSize,
                            cancelResult.messageId()
                    );
                }
            }
        }

        // Normal unkeen flow - no active session or user is not a participant
        QueueOutcome outcome = queueService.leaveQueue(userId, channelId);
        Set<Long> queueSnapshot = queueService.showQueue(channelId);
        int maxQueueSize = queueService.showMaxQueueSize(channelId);

        return switch (outcome) {
            case DEQUEUED -> new PlayerLeftQueueEventV2(context, queueSnapshot, maxQueueSize, false);
            case NOT_IN_QUEUE -> new PlayerNotInQueueEventV2(context);
            default -> throw new IllegalStateException("Unexpected queue outcome: " + outcome);
        };
    }
}

