package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolver;
import io.github.jnicog.discord.spanner.bot.event.checkin.PlayerAlreadyCheckedInEvent;
import io.github.jnicog.discord.spanner.bot.notification.MessageFormatterService;
import org.springframework.stereotype.Component;

/**
 *  ResponseResolver for PlayerAlreadyCheckedInEvent.
 */
@Component
public class PlayerAlreadyCheckedInResponseResolver implements ResponseResolver<PlayerAlreadyCheckedInEvent> {

    private final MessageFormatterService messageFormatter;

    public PlayerAlreadyCheckedInResponseResolver(MessageFormatterService messageFormatter) {
        this.messageFormatter = messageFormatter;
    }

    @Override
    public InteractionResponse resolve(PlayerAlreadyCheckedInEvent event) {
        return new InteractionResponse.EphemeralReply(messageFormatter.formatAlreadyCheckedIn());
    }
}

