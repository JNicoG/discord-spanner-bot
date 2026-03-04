package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolver;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManResignErrorEvent;
import org.springframework.stereotype.Component;

@Component
public class TenManResignErrorResponseResolver implements ResponseResolver<TenManResignErrorEvent> {

    @Override
    public InteractionResponse resolve(TenManResignErrorEvent event) {
        return new InteractionResponse.EphemeralReply(event.getMessage());
    }
}
