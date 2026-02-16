package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolver;
import io.github.jnicog.discord.spanner.bot.event.checkin.CheckInCancelledEvent;
import io.github.jnicog.discord.spanner.bot.notification.MessageFormatterService;
import org.springframework.stereotype.Component;

/**
 *  ResponseResolver for CheckInCancelledEvent.
 * Formats the cancellation message and removes buttons from the check-in message.
 */
@Component
public class CheckInCancelledResponseResolver implements ResponseResolver<CheckInCancelledEvent> {

    private final MessageFormatterService messageFormatter;

    public CheckInCancelledResponseResolver(MessageFormatterService messageFormatter) {
        this.messageFormatter = messageFormatter;
    }

    @Override
    public InteractionResponse resolve(CheckInCancelledEvent event) {
        String message = messageFormatter.formatCheckInCancelled(
                event.getContext().userId(),
                event.getRemainingUsersForQueue(),
                event.getMaxQueueSize()
        );
        return new InteractionResponse.EditButtonMessageAndClearComponents(message);
    }
}

