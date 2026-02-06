package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolver;
import io.github.jnicog.discord.spanner.bot.event.queue.QueueAlreadyFullEvent;
import io.github.jnicog.discord.spanner.bot.notification.MessageFormatterService;
import org.springframework.stereotype.Component;

/**
 *  ResponseResolver for QueueAlreadyFullEvent.
 */
@Component
public class QueueAlreadyFullResponseResolver implements ResponseResolver<QueueAlreadyFullEvent> {

    private final MessageFormatterService messageFormatter;

    public QueueAlreadyFullResponseResolver(MessageFormatterService messageFormatter) {
        this.messageFormatter = messageFormatter;
    }

    @Override
    public InteractionResponse resolve(QueueAlreadyFullEvent event) {
        return new InteractionResponse.EphemeralReply(messageFormatter.formatQueueFull());
    }
}

