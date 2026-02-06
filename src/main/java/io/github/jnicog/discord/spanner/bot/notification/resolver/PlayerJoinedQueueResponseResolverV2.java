package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolverV2;
import io.github.jnicog.discord.spanner.bot.event.queue.PlayerJoinedQueueEventV2;
import io.github.jnicog.discord.spanner.bot.notification.MessageFormatterService;
import org.springframework.stereotype.Component;

/**
 * V2 ResponseResolver for PlayerJoinedQueueEventV2.
 */
@Component
public class PlayerJoinedQueueResponseResolverV2 implements ResponseResolverV2<PlayerJoinedQueueEventV2> {

    private final MessageFormatterService messageFormatter;

    public PlayerJoinedQueueResponseResolverV2(MessageFormatterService messageFormatter) {
        this.messageFormatter = messageFormatter;
    }

    @Override
    public InteractionResponse resolve(PlayerJoinedQueueEventV2 event) {
        String message = messageFormatter.formatPlayerJoinedQueue(
                event.getContext().userId(),
                event.getUpdatedQueueSnapshot(),
                event.getMaxQueueSize()
        );

        return new InteractionResponse.PublicReply(message);
    }
}

