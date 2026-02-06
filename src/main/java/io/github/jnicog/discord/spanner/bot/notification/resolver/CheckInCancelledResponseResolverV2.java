package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolverV2;
import io.github.jnicog.discord.spanner.bot.event.checkin.CheckInCancelledEventV2;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * V2 ResponseResolver for CheckInCancelledEventV2.
 * Formats the cancellation message and removes buttons from the check-in message.
 */
@Component
public class CheckInCancelledResponseResolverV2 implements ResponseResolverV2<CheckInCancelledEventV2> {

    @Override
    public InteractionResponse resolve(CheckInCancelledEventV2 event) {
        long cancellingUserId = event.getContext().userId();
        var remainingQueue = event.getRemainingUsersForQueue();
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

        // Use UpdateOriginalMessageAndClearComponents to remove buttons
        return new InteractionResponse.UpdateOriginalMessageAndClearComponents(message);
    }
}

