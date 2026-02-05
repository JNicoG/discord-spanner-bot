package io.github.jnicog.discord.spanner.bot.notification.handler.queue;

import io.github.jnicog.discord.spanner.bot.event.queue.PlayerAlreadyQueuedEvent;
import io.github.jnicog.discord.spanner.bot.notification.handler.CommandEventNotificationHandler;
import org.springframework.stereotype.Component;

@Component
public class PlayerAlreadyQueuedEventNotificationHandler implements CommandEventNotificationHandler<PlayerAlreadyQueuedEvent> {

    @Override
    public void handle(PlayerAlreadyQueuedEvent event) {
        String message = "Unable to join the queue. The queue is already full!";
        event.getContext().interactionResponder().replyEphemeral(message);
    }
}
