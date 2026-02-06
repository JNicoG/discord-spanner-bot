package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolver;
import io.github.jnicog.discord.spanner.bot.event.checkin.ExpiredSessionCheckInEvent;
import io.github.jnicog.discord.spanner.bot.notification.MessageFormatterService;
import org.springframework.stereotype.Component;

/**
 *  ResponseResolver for ExpiredSessionCheckInEvent.
 */
@Component
public class ExpiredSessionCheckInResponseResolver implements ResponseResolver<ExpiredSessionCheckInEvent> {

    private final MessageFormatterService messageFormatter;

    public ExpiredSessionCheckInResponseResolver(MessageFormatterService messageFormatter) {
        this.messageFormatter = messageFormatter;
    }

    @Override
    public InteractionResponse resolve(ExpiredSessionCheckInEvent event) {
        return new InteractionResponse.EphemeralReply(messageFormatter.formatExpiredSession());
    }
}

