package io.github.jnicog.discord.spanner.bot.command.handler;

import io.github.jnicog.discord.spanner.bot.checkin.CancelResult;
import io.github.jnicog.discord.spanner.bot.checkin.CheckInService;
import io.github.jnicog.discord.spanner.bot.command.ButtonInteractionContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import io.github.jnicog.discord.spanner.bot.event.checkin.CheckInCancelledEvent;
import io.github.jnicog.discord.spanner.bot.event.checkin.NoActiveSessionEvent;
import io.github.jnicog.discord.spanner.bot.event.checkin.UnauthorisedCheckInEvent;
import io.github.jnicog.discord.spanner.bot.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handler for the check-in cancel/spanner button.
 * Cancels the check-in session. The cancelling user is removed from the queue,
 * while remaining users stay in the queue.
 */
@Component
public class CancelCheckInButtonHandler implements ButtonCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CancelCheckInButtonHandler.class);

    private final CheckInService checkInService;
    private final QueueService queueService;

    public CancelCheckInButtonHandler(CheckInService checkInService, QueueService queueService) {
        this.checkInService = checkInService;
        this.queueService = queueService;
    }

    @Override
    public String getCommandName() {
        return "checkInSpanner";
    }

    @Override
    public AbstractCommandResult<?> handleCommand(ButtonInteractionContext context) {
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

                yield new CheckInCancelledEvent(
                        context,
                        buttonMessageId,
                        queueSnapshot,
                        maxQueueSize
                );
            }
            case UNAUTHORISED -> new UnauthorisedCheckInEvent(context);
            case NO_ACTIVE_SESSION -> new NoActiveSessionEvent(context);
            default -> throw new IllegalStateException("Unexpected cancel result: " + cancelResult.result());
        };
    }
}

