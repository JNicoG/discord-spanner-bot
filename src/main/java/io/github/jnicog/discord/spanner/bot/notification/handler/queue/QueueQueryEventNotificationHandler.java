package io.github.jnicog.discord.spanner.bot.notification.handler.queue;

import io.github.jnicog.discord.spanner.bot.event.queue.QueueQueryEvent;
import io.github.jnicog.discord.spanner.bot.notification.handler.CommandEventNotificationHandler;
import org.springframework.stereotype.Component;

/**
 * @deprecated V1 notification handler. Use V2 response resolvers instead.
 * @see io.github.jnicog.discord.spanner.bot.notification.resolver.QueueQueryResponseResolverV2
 */
@Deprecated
// @Component - Disabled in favor of V2 response resolvers
public class QueueQueryEventNotificationHandler implements CommandEventNotificationHandler<QueueQueryEvent> {

    @Override
    public void handle(QueueQueryEvent event) {
        String currentQueue = event.getCurrentQueueSnapshot().stream()
                .map(id -> String.format("<@%s>", id))
                .collect(java.util.stream.Collectors.joining(", "));

        String message = String.format(
                "Current queue: %s",
                event.getCurrentQueueSnapshot().isEmpty() ? "No players in queue" : currentQueue
        );
        event.getContext().interactionResponder().replyPublic(message);
    }

}
