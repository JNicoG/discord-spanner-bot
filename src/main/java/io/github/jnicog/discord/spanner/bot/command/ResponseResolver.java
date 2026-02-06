package io.github.jnicog.discord.spanner.bot.command;

import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

/**
 * V1 response resolver that works with V1 events.
 *
 * @param <T> The type of command result this handler processes
 * @deprecated Use {@link ResponseResolverV2} instead which works with V2 JDA-free events.
 */
@Deprecated
@FunctionalInterface
public interface ResponseResolver<T extends AbstractCommandResult<?>> {

    /**
     * Resolve the appropriate response for the given command result.
     *
     * @param event The command result to process
     * @return The interaction response to send
     */
    InteractionResponse resolve(T event);
}
