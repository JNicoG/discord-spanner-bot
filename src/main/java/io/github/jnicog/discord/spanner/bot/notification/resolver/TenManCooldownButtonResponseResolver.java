package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolver;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManCooldownButtonEvent;
import org.springframework.stereotype.Component;

@Component
public class TenManCooldownButtonResponseResolver implements ResponseResolver<TenManCooldownButtonEvent> {

    @Override
    public InteractionResponse resolve(TenManCooldownButtonEvent event) {
        long mins = event.getRemainingSeconds() / 60;
        long secs = event.getRemainingSeconds() % 60;
        String timeLeft = mins > 0 ? mins + "m " + secs + "s" : secs + "s";
        return new InteractionResponse.PublicReply(
                "Easy there, <@" + event.getUserId() + ">! 🛑 You just changed your sign-up. Wait " + timeLeft + " before changing it again.");
    }
}
