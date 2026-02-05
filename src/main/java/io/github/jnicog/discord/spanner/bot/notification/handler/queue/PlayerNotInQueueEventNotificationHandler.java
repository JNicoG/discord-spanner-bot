package io.github.jnicog.discord.spanner.bot.notification.handler.queue;

import io.github.jnicog.discord.spanner.bot.event.queue.PlayerNotInQueueEvent;
import io.github.jnicog.discord.spanner.bot.notification.handler.CommandEventNotificationHandler;
import org.springframework.stereotype.Component;

@Component
public class PlayerNotInQueueEventNotificationHandler implements CommandEventNotificationHandler<PlayerNotInQueueEvent> {

    @Override
    public void handle(PlayerNotInQueueEvent event) {
        String message = "Cannot leave queue. You are not currently in the queue!";
        event.getContext().interactionResponder().replyEphemeral(message);
    }
}
