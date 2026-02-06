package io.github.jnicog.discord.spanner.bot.event.listener;

import io.github.jnicog.discord.spanner.bot.event.checkin.UnkeenDuringCheckInEventV2;
import io.github.jnicog.discord.spanner.bot.notification.CheckInMessageGateway;
import io.github.jnicog.discord.spanner.bot.notification.MessageFormatterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Listener that updates the check-in message when a user cancels via /unkeen.
 */
@Component
public class UnkeenDuringCheckInEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnkeenDuringCheckInEventListener.class);

    private final CheckInMessageGateway checkInMessageGateway;
    private final MessageFormatterService messageFormatter;

    public UnkeenDuringCheckInEventListener(CheckInMessageGateway checkInMessageGateway,
                                            MessageFormatterService messageFormatter) {
        this.checkInMessageGateway = checkInMessageGateway;
        this.messageFormatter = messageFormatter;
    }

    @EventListener
    @Order(2)
    public void onUnkeenDuringCheckIn(UnkeenDuringCheckInEventV2 event) {
        long channelId = event.getContext().channelId();
        long messageId = event.getCheckInMessageId();

        String message = messageFormatter.formatCheckInCancelled(
                event.getContext().userId(),
                event.getRemainingUsersInQueue(),
                event.getMaxQueueSize()
        );

        // Update the check-in message and remove buttons
        checkInMessageGateway.updateCheckInMessageAndClearButtons(channelId, messageId, message)
                .exceptionally(ex -> {
                    LOGGER.error("Failed to update check-in message for channel {}: {}", channelId, ex.getMessage());
                    return null;
                });
    }
}

