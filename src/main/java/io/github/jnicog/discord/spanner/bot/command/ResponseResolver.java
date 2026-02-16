package io.github.jnicog.discord.spanner.bot.command;

import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

/**
 * A handler that converts command results to responses.
 * This replaces the InteractionResponder pattern with pure functions.
 *
 * @param <T> The type of command result this handler processes
 */
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

