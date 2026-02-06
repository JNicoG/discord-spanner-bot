package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolver;
import io.github.jnicog.discord.spanner.bot.event.checkin.NoActiveSessionEvent;
import io.github.jnicog.discord.spanner.bot.notification.MessageFormatterService;
import org.springframework.stereotype.Component;

/**
 *  ResponseResolver for NoActiveSessionEvent.
 */
@Component
public class NoActiveSessionResponseResolver implements ResponseResolver<NoActiveSessionEvent> {

    private final MessageFormatterService messageFormatter;

    public NoActiveSessionResponseResolver(MessageFormatterService messageFormatter) {
        this.messageFormatter = messageFormatter;
    }

    @Override
    public InteractionResponse resolve(NoActiveSessionEvent event) {
        return new InteractionResponse.EphemeralReply(messageFormatter.formatNoActiveSession());
    }
}

