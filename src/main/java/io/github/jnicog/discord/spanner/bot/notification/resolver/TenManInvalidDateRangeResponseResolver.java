package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolver;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManInvalidDateRangeEvent;
import org.springframework.stereotype.Component;

@Component
public class TenManInvalidDateRangeResponseResolver implements ResponseResolver<TenManInvalidDateRangeEvent> {

    @Override
    public InteractionResponse resolve(TenManInvalidDateRangeEvent event) {
        return new InteractionResponse.EphemeralReply(event.getErrorMessage());
    }
}
