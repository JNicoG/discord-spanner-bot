package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolver;
import io.github.jnicog.discord.spanner.bot.event.queue.PlayerNotInQueueEvent;
import io.github.jnicog.discord.spanner.bot.notification.MessageFormatterService;
import org.springframework.stereotype.Component;

/**
 *  ResponseResolver for PlayerNotInQueueEvent.
 */
@Component
public class PlayerNotInQueueResponseResolver implements ResponseResolver<PlayerNotInQueueEvent> {

    private final MessageFormatterService messageFormatter;

    public PlayerNotInQueueResponseResolver(MessageFormatterService messageFormatter) {
        this.messageFormatter = messageFormatter;
    }

    @Override
    public InteractionResponse resolve(PlayerNotInQueueEvent event) {
        return new InteractionResponse.EphemeralReply(messageFormatter.formatNotInQueue());
    }
}

