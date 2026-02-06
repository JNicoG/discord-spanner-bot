package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolverV2;
import io.github.jnicog.discord.spanner.bot.event.checkin.PlayerAlreadyCheckedInEventV2;
import org.springframework.stereotype.Component;

/**
 * V2 ResponseResolver for PlayerAlreadyCheckedInEventV2.
 */
@Component
public class PlayerAlreadyCheckedInResponseResolverV2 implements ResponseResolverV2<PlayerAlreadyCheckedInEventV2> {

    @Override
    public InteractionResponse resolve(PlayerAlreadyCheckedInEventV2 event) {
        return new InteractionResponse.EphemeralReply("You have already checked in!");
    }
}

