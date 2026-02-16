package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolver;
import io.github.jnicog.discord.spanner.bot.event.queue.QueueQueryEvent;
import io.github.jnicog.discord.spanner.bot.notification.MessageFormatterService;
import org.springframework.stereotype.Component;

/**
 *  ResponseResolver for QueueQueryEvent.
 */
@Component
public class QueueQueryResponseResolver implements ResponseResolver<QueueQueryEvent> {

    private final MessageFormatterService messageFormatter;

    public QueueQueryResponseResolver(MessageFormatterService messageFormatter) {
        this.messageFormatter = messageFormatter;
    }

    @Override
    public InteractionResponse resolve(QueueQueryEvent event) {
        String message = messageFormatter.formatQueueStatus(
                event.getCurrentQueueSnapshot(),
                event.getMaxQueueSize()
        );

        return new InteractionResponse.PublicReply(message);
    }
}

