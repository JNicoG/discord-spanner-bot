package io.github.jnicog.discord.spanner.bot.notification.handler.queue;

import io.github.jnicog.discord.spanner.bot.event.queue.PlayerAlreadyQueuedEvent;
import io.github.jnicog.discord.spanner.bot.notification.handler.CommandEventNotificationHandler;
import org.springframework.stereotype.Component;

/**
 * @deprecated V1 notification handler. Use V2 response resolvers instead.
 * @see io.github.jnicog.discord.spanner.bot.notification.resolver.PlayerAlreadyQueuedResponseResolverV2
 */
@Deprecated
// @Component - Disabled in favor of V2 response resolvers
public class PlayerAlreadyQueuedEventNotificationHandler implements CommandEventNotificationHandler<PlayerAlreadyQueuedEvent> {

    @Override
    public void handle(PlayerAlreadyQueuedEvent event) {
        String message = "Unable to join the queue. The queue is already full!";
        event.getContext().interactionResponder().replyEphemeral(message);
    }
}
