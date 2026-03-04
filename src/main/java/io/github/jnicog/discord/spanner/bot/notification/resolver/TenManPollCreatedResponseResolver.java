package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolver;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManPollCreatedEvent;
import org.springframework.stereotype.Component;

@Component
public class TenManPollCreatedResponseResolver implements ResponseResolver<TenManPollCreatedEvent> {

    @Override
    public InteractionResponse resolve(TenManPollCreatedEvent event) {
        return new InteractionResponse.EphemeralReply("Poll created!");
    }
}
