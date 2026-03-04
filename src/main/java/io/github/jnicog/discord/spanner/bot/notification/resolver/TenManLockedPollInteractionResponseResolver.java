package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolver;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManLockedPollInteractionEvent;
import org.springframework.stereotype.Component;

@Component
public class TenManLockedPollInteractionResponseResolver implements ResponseResolver<TenManLockedPollInteractionEvent> {

    @Override
    public InteractionResponse resolve(TenManLockedPollInteractionEvent event) {
        return new InteractionResponse.EphemeralReply(event.getMessage());
    }
}
