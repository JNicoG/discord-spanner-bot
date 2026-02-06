package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolverV2;
import io.github.jnicog.discord.spanner.bot.event.checkin.UnauthorisedCheckInEventV2;
import org.springframework.stereotype.Component;

/**
 * V2 ResponseResolver for UnauthorisedCheckInEventV2.
 */
@Component
public class UnauthorisedCheckInResponseResolverV2 implements ResponseResolverV2<UnauthorisedCheckInEventV2> {

    @Override
    public InteractionResponse resolve(UnauthorisedCheckInEventV2 event) {
        return new InteractionResponse.EphemeralReply("You are not authorised to check in for this session.");
    }
}

