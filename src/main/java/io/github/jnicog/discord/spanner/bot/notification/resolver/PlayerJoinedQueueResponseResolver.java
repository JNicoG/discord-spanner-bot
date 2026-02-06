package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolver;
import io.github.jnicog.discord.spanner.bot.event.queue.PlayerJoinedQueueEvent;
import io.github.jnicog.discord.spanner.bot.notification.MessageFormatterService;
import org.springframework.stereotype.Component;

/**
 *  ResponseResolver for PlayerJoinedQueueEvent.
 */
@Component
public class PlayerJoinedQueueResponseResolver implements ResponseResolver<PlayerJoinedQueueEvent> {

    private final MessageFormatterService messageFormatter;

    public PlayerJoinedQueueResponseResolver(MessageFormatterService messageFormatter) {
        this.messageFormatter = messageFormatter;
    }

    @Override
    public InteractionResponse resolve(PlayerJoinedQueueEvent event) {
        String message = messageFormatter.formatPlayerJoinedQueue(
                event.getContext().userId(),
                event.getUpdatedQueueSnapshot(),
                event.getMaxQueueSize()
        );

        return new InteractionResponse.PublicReply(message);
    }
}

