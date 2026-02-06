package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolverV2;
import io.github.jnicog.discord.spanner.bot.event.checkin.UnauthorisedCheckInEventV2;
import io.github.jnicog.discord.spanner.bot.notification.MessageFormatterService;
import org.springframework.stereotype.Component;

/**
 * V2 ResponseResolver for UnauthorisedCheckInEventV2.
 */
@Component
public class UnauthorisedCheckInResponseResolverV2 implements ResponseResolverV2<UnauthorisedCheckInEventV2> {

    private final MessageFormatterService messageFormatter;

    public UnauthorisedCheckInResponseResolverV2(MessageFormatterService messageFormatter) {
        this.messageFormatter = messageFormatter;
    }

    @Override
    public InteractionResponse resolve(UnauthorisedCheckInEventV2 event) {
        return new InteractionResponse.EphemeralReply(messageFormatter.formatUnauthorisedCheckIn());
    }
}

