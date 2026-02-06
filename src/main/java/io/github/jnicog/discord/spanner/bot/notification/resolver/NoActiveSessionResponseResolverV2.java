package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolverV2;
import io.github.jnicog.discord.spanner.bot.event.checkin.NoActiveSessionEventV2;
import org.springframework.stereotype.Component;

/**
 * V2 ResponseResolver for NoActiveSessionEventV2.
 */
@Component
public class NoActiveSessionResponseResolverV2 implements ResponseResolverV2<NoActiveSessionEventV2> {

    @Override
    public InteractionResponse resolve(NoActiveSessionEventV2 event) {
        return new InteractionResponse.EphemeralReply("There is no active check-in session.");
    }
}

