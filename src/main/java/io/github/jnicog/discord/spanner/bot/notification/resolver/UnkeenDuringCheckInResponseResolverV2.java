package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolverV2;
import io.github.jnicog.discord.spanner.bot.event.checkin.UnkeenDuringCheckInEventV2;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * V2 ResponseResolver for UnkeenDuringCheckInEventV2.
 * Formats the "left the queue" message when a user uses /unkeen during check-in.
 * The check-in cancellation message is handled separately by the event listener
 * which updates the original check-in message.
 */
@Component
public class UnkeenDuringCheckInResponseResolverV2 implements ResponseResolverV2<UnkeenDuringCheckInEventV2> {

    @Override
    public InteractionResponse resolve(UnkeenDuringCheckInEventV2 event) {
        long userId = event.getContext().userId();
        var remainingQueue = event.getRemainingUsersInQueue();
        int maxQueueSize = event.getMaxQueueSize();

        String currentQueue = remainingQueue.stream()
                .map(id -> String.format("<@%s>", id))
                .collect(Collectors.joining(", "));

        String message = String.format(
                "<@%s> has left the queue! [%d/%d]\n Current queue: %s",
                userId,
                remainingQueue.size(),
                maxQueueSize,
                remainingQueue.isEmpty() ? "No players in queue" : currentQueue
        );

        return new InteractionResponse.PublicReply(message);
    }
}

