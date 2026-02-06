package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolverV2;
import io.github.jnicog.discord.spanner.bot.event.queue.PlayerAlreadyQueuedEventV2;
import org.springframework.stereotype.Component;

/**
 * V2 ResponseResolver for PlayerAlreadyQueuedEventV2.
 */
@Component
public class PlayerAlreadyQueuedResponseResolverV2 implements ResponseResolverV2<PlayerAlreadyQueuedEventV2> {

    @Override
    public InteractionResponse resolve(PlayerAlreadyQueuedEventV2 event) {
        return new InteractionResponse.EphemeralReply("You are already in the queue!");
    }
}

