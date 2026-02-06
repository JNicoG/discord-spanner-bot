package io.github.jnicog.discord.spanner.bot.command.handler;

import io.github.jnicog.discord.spanner.bot.checkin.CheckInAttemptResult;
import io.github.jnicog.discord.spanner.bot.checkin.CheckInService;
import io.github.jnicog.discord.spanner.bot.command.ButtonInteractionContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResultV2;
import io.github.jnicog.discord.spanner.bot.event.checkin.ExpiredSessionCheckInEventV2;
import io.github.jnicog.discord.spanner.bot.event.checkin.NoActiveSessionEventV2;
import io.github.jnicog.discord.spanner.bot.event.checkin.PlayerAlreadyCheckedInEventV2;
import io.github.jnicog.discord.spanner.bot.event.checkin.PlayerCheckInEventV2;
import io.github.jnicog.discord.spanner.bot.event.checkin.UnauthorisedCheckInEventV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * V2 handler for the check-in button.
 * Uses ButtonInteractionContext and returns V2 events.
 */
@Component
public class CheckInButtonHandlerV2 implements ButtonCommandHandlerV2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckInButtonHandlerV2.class);

    private final CheckInService checkInService;

    public CheckInButtonHandlerV2(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    @Override
    public String getCommandName() {
        return "checkInAccept";
    }

    @Override
    public AbstractCommandResultV2<?> handleCommand(ButtonInteractionContext context) {
        long userId = context.userId();
        long channelId = context.channelId();
        long buttonMessageId = context.messageId();

        CheckInAttemptResult checkInResult = checkInService.userCheckIn(channelId, userId, buttonMessageId);

        return switch (checkInResult) {
            case CHECKED_IN -> {
                Map<Long, Boolean> updatedCheckInSnapshot = checkInService.getUpdatedCheckInSnapshot(channelId);
                yield new PlayerCheckInEventV2(context, updatedCheckInSnapshot, buttonMessageId);
            }
            case ALREADY_CHECKED_IN -> new PlayerAlreadyCheckedInEventV2(context);
            case UNAUTHORISED -> new UnauthorisedCheckInEventV2(context);
            case NO_ACTIVE_SESSION -> new NoActiveSessionEventV2(context);
            case EXPIRED_SESSION -> new ExpiredSessionCheckInEventV2(context);
            default -> throw new IllegalStateException("Unexpected check-in result: " + checkInResult);
        };
    }
}

