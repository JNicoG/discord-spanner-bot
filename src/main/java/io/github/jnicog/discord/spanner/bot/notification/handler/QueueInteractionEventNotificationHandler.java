package io.github.jnicog.discord.spanner.bot.notification.handler;

import io.github.jnicog.discord.spanner.bot.event.QueueInteractionEvent;
import io.github.jnicog.discord.spanner.bot.queue.QueueOutcome;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class QueueInteractionEventNotificationHandler implements CommandEventNotificationHandler<QueueInteractionEvent> {

    public QueueInteractionEventNotificationHandler() {
        // Do not instantiate
    }

    @Override
    public Class<QueueInteractionEvent> handledEventType() {
        return QueueInteractionEvent.class;
    }

    @Override
    public void handle(QueueInteractionEvent event) {
        QueueOutcome outcome = event.getQueueOutcome();

        switch (outcome) {
            case ALREADY_QUEUED -> event.getInteractionResponder().replyEphemeral("You are already in the queue!");
            case QUEUE_FULL -> event.getInteractionResponder().replyEphemeral("The queue is already full!");
            case NOT_IN_QUEUE -> event.getInteractionResponder().replyEphemeral("You are not in the queue!");
            case ENQUEUED, DEQUEUED -> {
                String user = String.format("<@%s>", event.getUserId());
                String currentQueue = event.getQueueSnapshot().stream()
                        .map(id -> String.format("<@%s>", id))
                        .collect(Collectors.joining(", "));

                String message = String.format(
                        "%s has %s the queue! [%d/%d]\n Current queue: %s",
                        user,
                        outcome == QueueOutcome.ENQUEUED ? "joined" : "left",
                        event.getQueueSnapshot().size(),
                        event.getMaxQueueSize(),
                        event.getQueueSnapshot().isEmpty()? "No players in queue" : currentQueue
                );
                event.getInteractionResponder().replyPublic(message);
            }
            case QUERY -> {
                String currentQueue = event.getQueueSnapshot().stream()
                        .map(id -> String.format("<@%s>", id))
                        .collect(Collectors.joining(", "));

                String message = String.format(
                        "Current queue [%d/%d]: %s",
                        event.getQueueSnapshot().size(),
                        event.getMaxQueueSize(),
                        event.getQueueSnapshot().isEmpty()? "No players in queue" : currentQueue
                );
                event.getInteractionResponder().replyPublic(message);
            }
        }
    }

}
