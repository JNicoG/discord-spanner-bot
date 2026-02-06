package io.github.jnicog.discord.spanner.bot.notification.handler.queue;

import io.github.jnicog.discord.spanner.bot.event.queue.QueueAlreadyFullEvent;
import io.github.jnicog.discord.spanner.bot.notification.handler.CommandEventNotificationHandler;
import org.springframework.stereotype.Component;

/**
 * @deprecated V1 notification handler. Use V2 response resolvers instead.
 * @see io.github.jnicog.discord.spanner.bot.notification.resolver.QueueAlreadyFullResponseResolverV2
 */
@Deprecated
// @Component - Disabled in favor of V2 response resolvers
public class QueueAlreadyFullEventNotificationHandler implements CommandEventNotificationHandler<QueueAlreadyFullEvent> {

    @Override
    public void handle(QueueAlreadyFullEvent event) {
        String message = "You are already in the queue!";
        event.getContext().interactionResponder().replyEphemeral(message);
    }
}
