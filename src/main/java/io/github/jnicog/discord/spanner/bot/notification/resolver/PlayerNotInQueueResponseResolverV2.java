package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolverV2;
import io.github.jnicog.discord.spanner.bot.event.queue.PlayerNotInQueueEventV2;
import org.springframework.stereotype.Component;

/**
 * V2 ResponseResolver for PlayerNotInQueueEventV2.
 */
@Component
public class PlayerNotInQueueResponseResolverV2 implements ResponseResolverV2<PlayerNotInQueueEventV2> {

    @Override
    public InteractionResponse resolve(PlayerNotInQueueEventV2 event) {
        return new InteractionResponse.EphemeralReply("Cannot leave queue. You are not currently in the queue!");
    }
}

