package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolver;
import io.github.jnicog.discord.spanner.bot.event.queue.PlayerAlreadyQueuedEvent;
import io.github.jnicog.discord.spanner.bot.notification.MessageFormatterService;
import org.springframework.stereotype.Component;

/**
 *  ResponseResolver for PlayerAlreadyQueuedEvent.
 */
@Component
public class PlayerAlreadyQueuedResponseResolver implements ResponseResolver<PlayerAlreadyQueuedEvent> {

    private final MessageFormatterService messageFormatter;

    public PlayerAlreadyQueuedResponseResolver(MessageFormatterService messageFormatter) {
        this.messageFormatter = messageFormatter;
    }

    @Override
    public InteractionResponse resolve(PlayerAlreadyQueuedEvent event) {
        return new InteractionResponse.EphemeralReply(messageFormatter.formatAlreadyInQueue());
    }
}

