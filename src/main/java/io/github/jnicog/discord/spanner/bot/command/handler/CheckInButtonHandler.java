package io.github.jnicog.discord.spanner.bot.command.handler;

import io.github.jnicog.discord.spanner.bot.checkin.CheckInAttemptResult;
import io.github.jnicog.discord.spanner.bot.checkin.CheckInService;
import io.github.jnicog.discord.spanner.bot.command.ButtonInteractionContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import io.github.jnicog.discord.spanner.bot.event.checkin.CheckInCompletedEvent;
import io.github.jnicog.discord.spanner.bot.event.checkin.ExpiredSessionCheckInEvent;
import io.github.jnicog.discord.spanner.bot.event.checkin.NoActiveSessionEvent;
import io.github.jnicog.discord.spanner.bot.event.checkin.PlayerAlreadyCheckedInEvent;
import io.github.jnicog.discord.spanner.bot.event.checkin.PlayerCheckInEvent;
import io.github.jnicog.discord.spanner.bot.event.checkin.UnauthorisedCheckInEvent;
import io.github.jnicog.discord.spanner.bot.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Handler for the check-in button.
 * Uses ButtonInteractionContext.
 */
@Component
public class CheckInButtonHandler implements ButtonCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckInButtonHandler.class);

    private final CheckInService checkInService;
    private final QueueService queueService;

    public CheckInButtonHandler(CheckInService checkInService, QueueService queueService) {
        this.checkInService = checkInService;
        this.queueService = queueService;
    }

    @Override
    public String getCommandName() {
        return "checkInAccept";
    }

    @Override
    public AbstractCommandResult<?> handleCommand(ButtonInteractionContext context) {
        long userId = context.userId();
        long channelId = context.channelId();
        long buttonMessageId = context.messageId();

        // Get the snapshot BEFORE check-in (in case session completes and is removed)
        Map<Long, Boolean> preCheckInSnapshot = null;
        if (checkInService.hasActiveSession(channelId)) {
            preCheckInSnapshot = checkInService.getUpdatedCheckInSnapshot(channelId);
        }

        CheckInAttemptResult checkInResult = checkInService.userCheckIn(channelId, userId, buttonMessageId);

        return switch (checkInResult) {
            case CHECKED_IN -> {
                Map<Long, Boolean> updatedCheckInSnapshot = checkInService.getUpdatedCheckInSnapshot(channelId);
                yield new PlayerCheckInEvent(context, updatedCheckInSnapshot, buttonMessageId);
            }
            case SESSION_COMPLETED -> {
                // All users have checked in - session is now removed
                // Clear the queue so users can /keen again
                queueService.clearQueue(channelId);
                LOGGER.info("Queue cleared for channel {} after successful check-in completion", channelId);

                // Use the pre-check-in snapshot and update the last user's status
                Map<Long, Boolean> finalSnapshot = preCheckInSnapshot != null
                    ? updateSnapshot(preCheckInSnapshot, userId)
                    : Map.of(userId, true);
                yield new CheckInCompletedEvent(context, finalSnapshot, buttonMessageId);
            }
            case ALREADY_CHECKED_IN -> new PlayerAlreadyCheckedInEvent(context);
            case UNAUTHORISED -> new UnauthorisedCheckInEvent(context);
            case NO_ACTIVE_SESSION -> new NoActiveSessionEvent(context);
            case EXPIRED_SESSION -> new ExpiredSessionCheckInEvent(context);
            default -> throw new IllegalStateException("Unexpected check-in result: " + checkInResult);
        };
    }

    private Map<Long, Boolean> updateSnapshot(Map<Long, Boolean> original, long userId) {
        var mutable = new java.util.HashMap<>(original);
        mutable.put(userId, true);
        return Map.copyOf(mutable);
    }
}

