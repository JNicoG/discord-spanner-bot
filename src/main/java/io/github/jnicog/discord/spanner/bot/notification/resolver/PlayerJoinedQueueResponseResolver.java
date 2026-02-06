package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolver;
import io.github.jnicog.discord.spanner.bot.event.queue.PlayerJoinedQueueEvent;

import java.util.stream.Collectors;

/**
 * @deprecated Use {@link PlayerJoinedQueueResponseResolverV2} instead which uses V2 events.
 */
@Deprecated
// @Component - Disabled in favor of V2 resolver
public class PlayerJoinedQueueResponseResolver implements ResponseResolver<PlayerJoinedQueueEvent> {

    @Override
    public InteractionResponse resolve(PlayerJoinedQueueEvent event) {
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
