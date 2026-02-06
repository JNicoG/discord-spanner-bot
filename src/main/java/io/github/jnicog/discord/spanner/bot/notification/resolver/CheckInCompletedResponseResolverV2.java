package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolverV2;
import io.github.jnicog.discord.spanner.bot.event.checkin.CheckInCompletedEventV2;
import io.github.jnicog.discord.spanner.bot.notification.MessageFormatterService;
import org.springframework.stereotype.Component;

/**
 * V2 ResponseResolver for CheckInCompletedEventV2.
 * Updates the check-in message to show completion and removes buttons.
 */
@Component
public class CheckInCompletedResponseResolverV2 implements ResponseResolverV2<CheckInCompletedEventV2> {

    private final MessageFormatterService messageFormatter;

    public CheckInCompletedResponseResolverV2(MessageFormatterService messageFormatter) {
        this.messageFormatter = messageFormatter;
    }

    @Override
    public InteractionResponse resolve(CheckInCompletedEventV2 event) {
        String message = messageFormatter.formatCheckInCompleted(event.getFinalCheckInSnapshot());
        return new InteractionResponse.UpdateOriginalMessageAndClearComponents(message);
    }
}

