package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolverV2;
import io.github.jnicog.discord.spanner.bot.event.queue.QueueQueryEventV2;
import io.github.jnicog.discord.spanner.bot.notification.MessageFormatterService;
import org.springframework.stereotype.Component;

/**
 * V2 ResponseResolver for QueueQueryEventV2.
 */
@Component
public class QueueQueryResponseResolverV2 implements ResponseResolverV2<QueueQueryEventV2> {

    private final MessageFormatterService messageFormatter;

    public QueueQueryResponseResolverV2(MessageFormatterService messageFormatter) {
        this.messageFormatter = messageFormatter;
    }

    @Override
    public InteractionResponse resolve(QueueQueryEventV2 event) {
        String message = messageFormatter.formatQueueStatus(
                event.getCurrentQueueSnapshot(),
                event.getMaxQueueSize()
        );

        return new InteractionResponse.PublicReply(message);
    }
}

