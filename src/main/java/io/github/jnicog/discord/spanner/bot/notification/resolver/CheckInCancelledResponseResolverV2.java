package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolverV2;
import io.github.jnicog.discord.spanner.bot.event.checkin.CheckInCancelledEventV2;
import io.github.jnicog.discord.spanner.bot.notification.MessageFormatterService;
import org.springframework.stereotype.Component;

/**
 * V2 ResponseResolver for CheckInCancelledEventV2.
 * Formats the cancellation message and removes buttons from the check-in message.
 */
@Component
public class CheckInCancelledResponseResolverV2 implements ResponseResolverV2<CheckInCancelledEventV2> {

    private final MessageFormatterService messageFormatter;

    public CheckInCancelledResponseResolverV2(MessageFormatterService messageFormatter) {
        this.messageFormatter = messageFormatter;
    }

    @Override
    public InteractionResponse resolve(CheckInCancelledEventV2 event) {
        String message = messageFormatter.formatCheckInCancelled(
                event.getContext().userId(),
                event.getRemainingUsersForQueue(),
                event.getMaxQueueSize()
        );
        return new InteractionResponse.UpdateOriginalMessageAndClearComponents(message);
    }
}

