package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolver;
import io.github.jnicog.discord.spanner.bot.event.checkin.UnauthorisedCheckInEvent;
import io.github.jnicog.discord.spanner.bot.notification.MessageFormatterService;
import org.springframework.stereotype.Component;

/**
 *  ResponseResolver for UnauthorisedCheckInEvent.
 */
@Component
public class UnauthorisedCheckInResponseResolver implements ResponseResolver<UnauthorisedCheckInEvent> {

    private final MessageFormatterService messageFormatter;

    public UnauthorisedCheckInResponseResolver(MessageFormatterService messageFormatter) {
        this.messageFormatter = messageFormatter;
    }

    @Override
    public InteractionResponse resolve(UnauthorisedCheckInEvent event) {
        return new InteractionResponse.EphemeralReply(messageFormatter.formatUnauthorisedCheckIn());
    }
}

