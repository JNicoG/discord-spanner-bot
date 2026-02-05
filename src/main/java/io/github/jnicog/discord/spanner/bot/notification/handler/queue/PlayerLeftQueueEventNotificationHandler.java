package io.github.jnicog.discord.spanner.bot.notification.handler.queue;

import io.github.jnicog.discord.spanner.bot.event.queue.PlayerLeftQueueEvent;
import io.github.jnicog.discord.spanner.bot.notification.handler.CommandEventNotificationHandler;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PlayerLeftQueueEventNotificationHandler implements CommandEventNotificationHandler<PlayerLeftQueueEvent> {

    @Override
    public void handle(PlayerLeftQueueEvent event) {
                String user = String.format("<@%s>", event.getContext().userId());
                String currentQueue = event.getUpdatedQueueSnapshot().stream()
                        .map(id -> String.format("<@%s>", id))
                        .collect(Collectors.joining(", "));

                String message = String.format(
                        "%s has left the queue! [%d/%d]\n Current queue: %s",
                        user,
                        event.getUpdatedQueueSnapshot().size(),
                        event.getMaxQueueSize(),
                        event.getUpdatedQueueSnapshot().isEmpty()? "No players in queue" : currentQueue
                );
                event.getContext().interactionResponder().replyPublic(message);
    }
}
