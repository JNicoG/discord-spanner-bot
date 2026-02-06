package io.github.jnicog.discord.spanner.bot.notification.handler.checkin;

import io.github.jnicog.discord.spanner.bot.event.checkin.NoActiveSessionEvent;
import io.github.jnicog.discord.spanner.bot.notification.handler.CommandEventNotificationHandler;
import org.springframework.stereotype.Component;

@Component
public class NoActiveSessionEventNotificationHandler implements CommandEventNotificationHandler<NoActiveSessionEvent> {

    @Override
    public void handle(NoActiveSessionEvent event) {
        event.getContext().interactionResponder().replyEphemeral("Failed to check-in: No active check-in session found.");
    }
}
