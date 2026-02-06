package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolverV2;
import io.github.jnicog.discord.spanner.bot.event.checkin.NoActiveSessionEventV2;
import io.github.jnicog.discord.spanner.bot.notification.MessageFormatterService;
import org.springframework.stereotype.Component;

/**
 * V2 ResponseResolver for NoActiveSessionEventV2.
 */
@Component
public class NoActiveSessionResponseResolverV2 implements ResponseResolverV2<NoActiveSessionEventV2> {

    private final MessageFormatterService messageFormatter;

    public NoActiveSessionResponseResolverV2(MessageFormatterService messageFormatter) {
        this.messageFormatter = messageFormatter;
    }

    @Override
    public InteractionResponse resolve(NoActiveSessionEventV2 event) {
        return new InteractionResponse.EphemeralReply(messageFormatter.formatNoActiveSession());
    }
}

