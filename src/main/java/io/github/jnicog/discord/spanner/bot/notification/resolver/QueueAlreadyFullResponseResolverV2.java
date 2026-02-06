package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolverV2;
import io.github.jnicog.discord.spanner.bot.event.queue.QueueAlreadyFullEventV2;
import io.github.jnicog.discord.spanner.bot.notification.MessageFormatterService;
import org.springframework.stereotype.Component;

/**
 * V2 ResponseResolver for QueueAlreadyFullEventV2.
 */
@Component
public class QueueAlreadyFullResponseResolverV2 implements ResponseResolverV2<QueueAlreadyFullEventV2> {

    private final MessageFormatterService messageFormatter;

    public QueueAlreadyFullResponseResolverV2(MessageFormatterService messageFormatter) {
        this.messageFormatter = messageFormatter;
    }

    @Override
    public InteractionResponse resolve(QueueAlreadyFullEventV2 event) {
        return new InteractionResponse.EphemeralReply(messageFormatter.formatQueueFull());
    }
}

