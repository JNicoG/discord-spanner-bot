package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolverV2;
import io.github.jnicog.discord.spanner.bot.event.queue.PlayerLeftQueueEventV2;
import io.github.jnicog.discord.spanner.bot.notification.MessageFormatterService;
import org.springframework.stereotype.Component;

/**
 * V2 ResponseResolver for PlayerLeftQueueEventV2.
 */
@Component
public class PlayerLeftQueueResponseResolverV2 implements ResponseResolverV2<PlayerLeftQueueEventV2> {

    private final MessageFormatterService messageFormatter;

    public PlayerLeftQueueResponseResolverV2(MessageFormatterService messageFormatter) {
        this.messageFormatter = messageFormatter;
    }

    @Override
    public InteractionResponse resolve(PlayerLeftQueueEventV2 event) {
        String message = messageFormatter.formatPlayerLeftQueue(
                event.getContext().userId(),
                event.getUpdatedQueueSnapshot(),
                event.getMaxQueueSize()
        );

        return new InteractionResponse.PublicReply(message);
    }
}

