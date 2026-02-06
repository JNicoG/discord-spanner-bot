package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolverV2;
import io.github.jnicog.discord.spanner.bot.event.checkin.ExpiredSessionCheckInEventV2;
import org.springframework.stereotype.Component;

/**
 * V2 ResponseResolver for ExpiredSessionCheckInEventV2.
 */
@Component
public class ExpiredSessionCheckInResponseResolverV2 implements ResponseResolverV2<ExpiredSessionCheckInEventV2> {

    @Override
    public InteractionResponse resolve(ExpiredSessionCheckInEventV2 event) {
        return new InteractionResponse.EphemeralReply("This check-in session is expired.");
    }
}

