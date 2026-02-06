package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolverV2;
import io.github.jnicog.discord.spanner.bot.event.queue.QueueQueryEventV2;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * V2 ResponseResolver for QueueQueryEventV2.
 */
@Component
public class QueueQueryResponseResolverV2 implements ResponseResolverV2<QueueQueryEventV2> {

    @Override
    public InteractionResponse resolve(QueueQueryEventV2 event) {
        String currentQueue = event.getCurrentQueueSnapshot().stream()
                .map(id -> String.format("<@%s>", id))
                .collect(Collectors.joining(", "));

        String message = String.format(
                "Current queue: %s [%d/%d]",
                event.getCurrentQueueSnapshot().isEmpty() ? "No players in queue" : currentQueue,
                event.getCurrentQueueSnapshot().size(),
                event.getMaxQueueSize()
        );

        return new InteractionResponse.PublicReply(message);
    }
}

