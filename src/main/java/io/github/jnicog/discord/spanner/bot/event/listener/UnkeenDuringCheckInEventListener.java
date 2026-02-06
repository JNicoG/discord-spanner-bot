package io.github.jnicog.discord.spanner.bot.event.listener;

import io.github.jnicog.discord.spanner.bot.event.checkin.UnkeenDuringCheckInEventV2;
import io.github.jnicog.discord.spanner.bot.notification.CheckInMessageGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Listener that updates the check-in message when a user cancels via /unkeen.
 */
@Component
public class UnkeenDuringCheckInEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnkeenDuringCheckInEventListener.class);

    private final CheckInMessageGateway checkInMessageGateway;

    public UnkeenDuringCheckInEventListener(CheckInMessageGateway checkInMessageGateway) {
        this.checkInMessageGateway = checkInMessageGateway;
    }

    @EventListener
    @Order(2)
    public void onUnkeenDuringCheckIn(UnkeenDuringCheckInEventV2 event) {
        long channelId = event.getContext().channelId();
        long messageId = event.getCheckInMessageId();
        long cancellingUserId = event.getContext().userId();
        var remainingQueue = event.getRemainingUsersInQueue();
        int maxQueueSize = event.getMaxQueueSize();

        String message;
        if (remainingQueue.isEmpty()) {
            message = String.format(
                    "Check-in cancelled by <@%d>\n" +
                    "No players remaining in queue.",
                    cancellingUserId
            );
        } else {
            String queueList = remainingQueue.stream()
                    .map(id -> String.format("<@%d>", id))
                    .collect(Collectors.joining(", "));

            message = String.format(
                    "Check-in cancelled by <@%d>\n" +
                    "Current queue: %s [%d/%d]",
                    cancellingUserId,
                    queueList,
                    remainingQueue.size(),
                    maxQueueSize
            );
        }

        // Update the check-in message and remove buttons
        checkInMessageGateway.updateCheckInMessageAndClearButtons(channelId, messageId, message)
                .exceptionally(ex -> {
                    LOGGER.error("Failed to update check-in message for channel {}: {}", channelId, ex.getMessage());
                    return null;
                });
    }
}

