package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolver;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManNoPollActiveEvent;
import org.springframework.stereotype.Component;

@Component
public class TenManNoPollActiveResponseResolver implements ResponseResolver<TenManNoPollActiveEvent> {

    @Override
    public InteractionResponse resolve(TenManNoPollActiveEvent event) {
        return new InteractionResponse.EphemeralReply("There is no active poll in this channel.");
    }
}
