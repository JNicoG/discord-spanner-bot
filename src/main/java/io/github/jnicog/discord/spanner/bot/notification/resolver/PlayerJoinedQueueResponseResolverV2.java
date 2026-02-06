package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolverV2;
import io.github.jnicog.discord.spanner.bot.event.queue.PlayerJoinedQueueEventV2;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * V2 ResponseResolver for PlayerJoinedQueueEventV2.
 */
@Component
public class PlayerJoinedQueueResponseResolverV2 implements ResponseResolverV2<PlayerJoinedQueueEventV2> {

    @Override
    public InteractionResponse resolve(PlayerJoinedQueueEventV2 event) {
        String user = String.format("<@%s>", event.getContext().userId());
        String currentQueue = event.getUpdatedQueueSnapshot().stream()
                .map(id -> String.format("<@%s>", id))
                .collect(Collectors.joining(", "));

        String message = String.format(
                "%s has joined the queue! [%d/%d]\n Current queue: %s",
                user,
                event.getUpdatedQueueSnapshot().size(),
                event.getMaxQueueSize(),
                event.getUpdatedQueueSnapshot().isEmpty() ? "No players in queue" : currentQueue
        );

        return new InteractionResponse.PublicReply(message);
    }
}

