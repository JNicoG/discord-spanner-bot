package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolver;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManPollCancelledEvent;
import org.springframework.stereotype.Component;

@Component
public class TenManPollCancelledResponseResolver implements ResponseResolver<TenManPollCancelledEvent> {

    @Override
    public InteractionResponse resolve(TenManPollCancelledEvent event) {
        return new InteractionResponse.EphemeralReply("Poll cancelled.");
    }
}
