package io.github.jnicog.discord.spanner.bot.command.handler;

import io.github.jnicog.discord.spanner.bot.checkin.CheckInAttemptResult;
import io.github.jnicog.discord.spanner.bot.checkin.CheckInService;
import io.github.jnicog.discord.spanner.bot.command.ButtonContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import io.github.jnicog.discord.spanner.bot.event.checkin.ExpiredSessionCheckInEvent;
import io.github.jnicog.discord.spanner.bot.event.checkin.NoActiveSessionEvent;
import io.github.jnicog.discord.spanner.bot.event.checkin.PlayerAlreadyCheckedInEvent;
import io.github.jnicog.discord.spanner.bot.event.checkin.PlayerCheckInEvent;
import io.github.jnicog.discord.spanner.bot.event.checkin.UnauthorisedCheckInEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @deprecated Use {@link CheckInButtonHandlerV2} instead.
 */
@Deprecated
// @Component - Disabled in favor of CheckInButtonHandlerV2
public class CheckInButtonHandler implements ButtonCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckInButtonHandler.class);

    private final CheckInService checkInService;

    public CheckInButtonHandler(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    @Override
    public String getCommandName() {
        return "checkInAccept";
    }

    @Override
    public boolean isEphemeral() {
        return false;
    }

    @Override
    public AbstractCommandResult<?> handleCommand(ButtonContext context) {
        long userId = context.userId();
        long channelId = context.channelId();
        long buttonMessageId = context.messageId();

        // These two need to be joined into one atomic operation
        CheckInAttemptResult checkInResult = checkInService.userCheckIn(channelId, userId, buttonMessageId);
        Map<Long, Boolean> updatedCheckInSnapshot = checkInService.getUpdatedCheckInSnapshot(channelId);

        return switch(checkInResult) {
            case CHECKED_IN -> new PlayerCheckInEvent(context, updatedCheckInSnapshot, buttonMessageId);
            case ALREADY_CHECKED_IN -> new PlayerAlreadyCheckedInEvent(context);
            case UNAUTHORISED -> new UnauthorisedCheckInEvent(context);
            case NO_ACTIVE_SESSION -> new NoActiveSessionEvent(context);
            case EXPIRED_SESSION -> new ExpiredSessionCheckInEvent(context);
            default -> throw new IllegalStateException("Unexpected check-in result: " + checkInResult);
        };
    }

}
