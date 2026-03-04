package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolver;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManRosterResignedSlashEvent;
import org.springframework.stereotype.Component;

@Component
public class TenManRosterResignedSlashResponseResolver implements ResponseResolver<TenManRosterResignedSlashEvent> {

    @Override
    public InteractionResponse resolve(TenManRosterResignedSlashEvent event) {
        return new InteractionResponse.EphemeralReply("You've been removed from the ten-man roster. +1 spanner 🔧");
    }
}
