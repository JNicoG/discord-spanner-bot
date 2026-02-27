package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolver;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManFillSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class TenManFillSuccessResponseResolver implements ResponseResolver<TenManFillSuccessEvent> {

    @Override
    public InteractionResponse resolve(TenManFillSuccessEvent event) {
        return new InteractionResponse.EphemeralReply("You've been added to the ten-man roster! 🎮");
    }
}
