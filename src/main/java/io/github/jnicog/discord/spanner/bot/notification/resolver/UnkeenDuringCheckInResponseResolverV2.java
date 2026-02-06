package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolverV2;
import io.github.jnicog.discord.spanner.bot.event.checkin.UnkeenDuringCheckInEventV2;
import io.github.jnicog.discord.spanner.bot.notification.MessageFormatterService;
import org.springframework.stereotype.Component;

/**
 * V2 ResponseResolver for UnkeenDuringCheckInEventV2.
 * Formats the "left the queue" message when a user uses /unkeen during check-in.
 * The check-in cancellation message is handled separately by the event listener
 * which updates the original check-in message.
 */
@Component
public class UnkeenDuringCheckInResponseResolverV2 implements ResponseResolverV2<UnkeenDuringCheckInEventV2> {

    private final MessageFormatterService messageFormatter;

    public UnkeenDuringCheckInResponseResolverV2(MessageFormatterService messageFormatter) {
        this.messageFormatter = messageFormatter;
    }

    @Override
    public InteractionResponse resolve(UnkeenDuringCheckInEventV2 event) {
        String message = messageFormatter.formatPlayerLeftQueue(
                event.getContext().userId(),
                event.getRemainingUsersInQueue(),
                event.getMaxQueueSize()
        );

        return new InteractionResponse.PublicReply(message);
    }
}

