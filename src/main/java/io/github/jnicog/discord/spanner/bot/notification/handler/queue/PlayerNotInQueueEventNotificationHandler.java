package io.github.jnicog.discord.spanner.bot.notification.handler.queue;

import io.github.jnicog.discord.spanner.bot.event.queue.PlayerNotInQueueEvent;
import io.github.jnicog.discord.spanner.bot.notification.handler.CommandEventNotificationHandler;
import org.springframework.stereotype.Component;

/**
 * @deprecated V1 notification handler. Use V2 response resolvers instead.
 * @see io.github.jnicog.discord.spanner.bot.notification.resolver.PlayerNotInQueueResponseResolverV2
 */
@Deprecated
// @Component - Disabled in favor of V2 response resolvers
public class PlayerNotInQueueEventNotificationHandler implements CommandEventNotificationHandler<PlayerNotInQueueEvent> {

    @Override
    public void handle(PlayerNotInQueueEvent event) {
        String message = "Cannot leave queue. You are not currently in the queue!";
        event.getContext().interactionResponder().replyEphemeral(message);
    }
}
