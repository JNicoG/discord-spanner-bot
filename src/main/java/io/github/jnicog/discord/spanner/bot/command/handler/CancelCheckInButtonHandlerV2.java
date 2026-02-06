package io.github.jnicog.discord.spanner.bot.command.handler;

import io.github.jnicog.discord.spanner.bot.checkin.CancelResult;
import io.github.jnicog.discord.spanner.bot.checkin.CheckInService;
import io.github.jnicog.discord.spanner.bot.command.ButtonInteractionContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResultV2;
import io.github.jnicog.discord.spanner.bot.event.checkin.CheckInCancelledEventV2;
import io.github.jnicog.discord.spanner.bot.event.checkin.NoActiveSessionEventV2;
import io.github.jnicog.discord.spanner.bot.event.checkin.UnauthorisedCheckInEventV2;
import io.github.jnicog.discord.spanner.bot.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * V2 handler for the check-in cancel/spanner button.
 * Cancels the check-in session. The cancelling user is removed from the queue,
 * while remaining users stay in the queue.
 */
@Component
public class CancelCheckInButtonHandlerV2 implements ButtonCommandHandlerV2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(CancelCheckInButtonHandlerV2.class);

    private final CheckInService checkInService;
    private final QueueService queueService;

    public CancelCheckInButtonHandlerV2(CheckInService checkInService, QueueService queueService) {
        this.checkInService = checkInService;
        this.queueService = queueService;
    }

    @Override
    public String getCommandName() {
        return "checkInSpanner";
    }

    @Override
    public AbstractCommandResultV2<?> handleCommand(ButtonInteractionContext context) {
        long userId = context.userId();
        long channelId = context.channelId();
        long buttonMessageId = context.messageId();

        LOGGER.info("User {} is cancelling check-in session in channel {}", userId, channelId);

        // Cancel the session and get remaining users
        CancelResult cancelResult = checkInService.cancelAndGetRemainingUsers(channelId, userId);

        return switch (cancelResult.result()) {
            case SESSION_CANCELLED -> {
                // Remove the cancelling user from the queue
                // (remaining users stay in the queue - they were never removed)
                queueService.leaveQueue(userId, channelId);
                LOGGER.debug("Removed cancelling user {} from queue in channel {}", userId, channelId);

                // Get the updated queue snapshot (excludes the cancelling user)
                var queueSnapshot = queueService.showQueue(channelId);
                int maxQueueSize = queueService.showMaxQueueSize(channelId);

                yield new CheckInCancelledEventV2(
                        context,
                        buttonMessageId,
                        queueSnapshot,
                        maxQueueSize
                );
            }
            case UNAUTHORISED -> new UnauthorisedCheckInEventV2(context);
            case NO_ACTIVE_SESSION -> new NoActiveSessionEventV2(context);
            default -> throw new IllegalStateException("Unexpected cancel result: " + cancelResult.result());
        };
    }
}

