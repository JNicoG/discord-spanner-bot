package io.github.jnicog.discord.spanner.bot.notification.handler.checkin;

import io.github.jnicog.discord.spanner.bot.event.checkin.CheckInTimeoutEvent;
import io.github.jnicog.discord.spanner.bot.notification.CheckInMessageGateway;
import io.github.jnicog.discord.spanner.bot.notification.MessageFormatterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Handles notification updates when a check-in session times out.
 * Updates the check-in message to show the timeout status and removes buttons.
 */
@Component
public class CheckInTimeoutEventNotificationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckInTimeoutEventNotificationHandler.class);

    private final CheckInMessageGateway checkInMessageGateway;
    private final MessageFormatterService messageFormatter;

    public CheckInTimeoutEventNotificationHandler(CheckInMessageGateway checkInMessageGateway,
                                                  MessageFormatterService messageFormatter) {
        this.checkInMessageGateway = checkInMessageGateway;
        this.messageFormatter = messageFormatter;
    }

    @EventListener
    public void onTimeout(CheckInTimeoutEvent event) {
        long channelId = event.getChannelId();
        long messageId = event.getMessageId();

        LOGGER.info("Handling CheckInTimeoutEvent for channel {}, updating message {}", channelId, messageId);

        String message = messageFormatter.formatCheckInTimeout(
                event.getUsersWhoDidNotCheckIn(),
                event.getUsersWhoCheckedIn()
        );

        checkInMessageGateway.updateCheckInMessageAndClearButtons(channelId, messageId, message)
                .exceptionally(ex -> {
                    LOGGER.error("Failed to update check-in message for timeout in channel {}, message {}: {}",
                            channelId, messageId, ex.getMessage());
                    return null;
                });
    }
}

