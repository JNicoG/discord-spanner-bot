package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolver;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManPollAlreadyActiveEvent;
import org.springframework.stereotype.Component;

@Component
public class TenManPollAlreadyActiveResponseResolver implements ResponseResolver<TenManPollAlreadyActiveEvent> {

    @Override
    public InteractionResponse resolve(TenManPollAlreadyActiveEvent event) {
        return new InteractionResponse.EphemeralReply("A poll is already active in this channel.");
    }
}
