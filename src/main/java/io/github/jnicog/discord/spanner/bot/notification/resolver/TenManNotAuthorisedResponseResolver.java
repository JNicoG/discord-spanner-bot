package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolver;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManNotAuthorisedEvent;
import org.springframework.stereotype.Component;

@Component
public class TenManNotAuthorisedResponseResolver implements ResponseResolver<TenManNotAuthorisedEvent> {

    @Override
    public InteractionResponse resolve(TenManNotAuthorisedEvent event) {
        return new InteractionResponse.EphemeralReply("You are not authorised to manage ten-man polls.");
    }
}
