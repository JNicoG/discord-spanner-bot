package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolver;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManFillErrorEvent;
import org.springframework.stereotype.Component;

@Component
public class TenManFillErrorResponseResolver implements ResponseResolver<TenManFillErrorEvent> {

    @Override
    public InteractionResponse resolve(TenManFillErrorEvent event) {
        return new InteractionResponse.EphemeralReply(event.getMessage());
    }
}
