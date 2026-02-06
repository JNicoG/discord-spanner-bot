package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolverV2;
import io.github.jnicog.discord.spanner.bot.event.checkin.PlayerCheckInEventV2;
import io.github.jnicog.discord.spanner.bot.notification.MessageFormatterService;
import org.springframework.stereotype.Component;

/**
 * V2 ResponseResolver for PlayerCheckInEventV2.
 * Updates the check-in message with the current check-in status.
 */
@Component
public class PlayerCheckInResponseResolverV2 implements ResponseResolverV2<PlayerCheckInEventV2> {

    private final MessageFormatterService messageFormatter;

    public PlayerCheckInResponseResolverV2(MessageFormatterService messageFormatter) {
        this.messageFormatter = messageFormatter;
    }

    @Override
    public InteractionResponse resolve(PlayerCheckInEventV2 event) {
        String message = messageFormatter.formatCheckInProgress(event.getUpdatedCheckInSnapshot());
        return new InteractionResponse.UpdateOriginalMessage(message);
    }
}

