package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolver;
import io.github.jnicog.discord.spanner.bot.event.checkin.PlayerCheckInEvent;
import io.github.jnicog.discord.spanner.bot.notification.MessageFormatterService;
import org.springframework.stereotype.Component;

/**
 *  ResponseResolver for PlayerCheckInEvent.
 * Updates the check-in message with the current check-in status.
 */
@Component
public class PlayerCheckInResponseResolver implements ResponseResolver<PlayerCheckInEvent> {

    private final MessageFormatterService messageFormatter;

    public PlayerCheckInResponseResolver(MessageFormatterService messageFormatter) {
        this.messageFormatter = messageFormatter;
    }

    @Override
    public InteractionResponse resolve(PlayerCheckInEvent event) {
        String message = messageFormatter.formatCheckInProgress(event.getUpdatedCheckInSnapshot());
        return new InteractionResponse.UpdateOriginalMessage(message);
    }
}

