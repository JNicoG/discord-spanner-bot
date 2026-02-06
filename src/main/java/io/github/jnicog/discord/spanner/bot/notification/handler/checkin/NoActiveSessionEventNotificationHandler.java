package io.github.jnicog.discord.spanner.bot.notification.handler.checkin;

import io.github.jnicog.discord.spanner.bot.event.checkin.NoActiveSessionEvent;
import io.github.jnicog.discord.spanner.bot.notification.handler.CommandEventNotificationHandler;
import org.springframework.stereotype.Component;

/**
 * @deprecated V1 notification handler. Use V2 response resolvers instead.
 * @see io.github.jnicog.discord.spanner.bot.notification.resolver.NoActiveSessionResponseResolverV2
 */
@Deprecated
// @Component - Disabled in favor of V2 response resolvers
public class NoActiveSessionEventNotificationHandler implements CommandEventNotificationHandler<NoActiveSessionEvent> {

    @Override
    public void handle(NoActiveSessionEvent event) {
        event.getContext().interactionResponder().replyEphemeral("Failed to check-in: No active check-in session found.");
    }
}
