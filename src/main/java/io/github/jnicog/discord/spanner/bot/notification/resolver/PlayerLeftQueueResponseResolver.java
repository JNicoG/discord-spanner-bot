package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolver;
import io.github.jnicog.discord.spanner.bot.event.queue.PlayerLeftQueueEvent;
import io.github.jnicog.discord.spanner.bot.notification.MessageFormatterService;
import org.springframework.stereotype.Component;

/**
 *  ResponseResolver for PlayerLeftQueueEvent.
 */
@Component
public class PlayerLeftQueueResponseResolver implements ResponseResolver<PlayerLeftQueueEvent> {

    private final MessageFormatterService messageFormatter;

    public PlayerLeftQueueResponseResolver(MessageFormatterService messageFormatter) {
        this.messageFormatter = messageFormatter;
    }

    @Override
    public InteractionResponse resolve(PlayerLeftQueueEvent event) {
        String message = messageFormatter.formatPlayerLeftQueue(
                event.getContext().userId(),
                event.getUpdatedQueueSnapshot(),
                event.getMaxQueueSize()
        );

        return new InteractionResponse.PublicReply(message);
    }
}

