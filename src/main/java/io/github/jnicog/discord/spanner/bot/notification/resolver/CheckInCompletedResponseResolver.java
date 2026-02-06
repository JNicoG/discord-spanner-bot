package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolver;
import io.github.jnicog.discord.spanner.bot.event.checkin.CheckInCompletedEvent;
import io.github.jnicog.discord.spanner.bot.notification.MessageFormatterService;
import org.springframework.stereotype.Component;

/**
 *  ResponseResolver for CheckInCompletedEvent.
 * Updates the check-in message to show completion and removes buttons.
 */
@Component
public class CheckInCompletedResponseResolver implements ResponseResolver<CheckInCompletedEvent> {

    private final MessageFormatterService messageFormatter;

    public CheckInCompletedResponseResolver(MessageFormatterService messageFormatter) {
        this.messageFormatter = messageFormatter;
    }

    @Override
    public InteractionResponse resolve(CheckInCompletedEvent event) {
        String message = messageFormatter.formatCheckInCompleted(event.getFinalCheckInSnapshot());
        return new InteractionResponse.UpdateOriginalMessageAndClearComponents(message);
    }
}

