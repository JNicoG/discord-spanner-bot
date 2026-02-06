package io.github.jnicog.discord.spanner.bot.notification.handler.queue;

import io.github.jnicog.discord.spanner.bot.event.queue.CheckInActiveEvent;
import io.github.jnicog.discord.spanner.bot.notification.handler.CommandEventNotificationHandler;
import org.springframework.stereotype.Component;

@Component
public class CheckInActiveEventNotificationHandler implements CommandEventNotificationHandler<CheckInActiveEvent> {

    @Override
    public void handle(CheckInActiveEvent event) {
        // Need to update this to cancel check-in session and remove player from queue.
        // For now, just let the buttons handle the logic and block this command from being used while check-in
        // session is active.
        String message = "Unable to leave the queue. Check-in session is currently active!";
        event.getContext().interactionResponder().replyEphemeral(message);
    }
}
