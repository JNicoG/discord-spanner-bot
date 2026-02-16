package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolver;
import io.github.jnicog.discord.spanner.bot.event.checkin.UnkeenDuringCheckInEvent;
import io.github.jnicog.discord.spanner.bot.notification.MessageFormatterService;
import org.springframework.stereotype.Component;

/**
 *  ResponseResolver for UnkeenDuringCheckInEvent.
 * Formats the "left the queue" message when a user uses /unkeen during check-in.
 * The check-in cancellation message is handled separately by the event listener
 * which updates the original check-in message.
 */
@Component
public class UnkeenDuringCheckInResponseResolver implements ResponseResolver<UnkeenDuringCheckInEvent> {

    private final MessageFormatterService messageFormatter;

    public UnkeenDuringCheckInResponseResolver(MessageFormatterService messageFormatter) {
        this.messageFormatter = messageFormatter;
    }

    @Override
    public InteractionResponse resolve(UnkeenDuringCheckInEvent event) {
        String message = messageFormatter.formatPlayerLeftQueue(
                event.getContext().userId(),
                event.getRemainingUsersInQueue(),
                event.getMaxQueueSize()
        );

        return new InteractionResponse.PublicReply(message);
    }
}

