package io.github.jnicog.discord.spanner.bot.event;

import io.github.jnicog.discord.spanner.bot.command.InteractionContext;

/**
 * V1 abstract command result that holds JDA-coupled context.
 *
 * @deprecated Use {@link AbstractCommandResultV2} instead.
 */
@Deprecated
public abstract class AbstractCommandResult<T extends InteractionContext> {

    private final T context;

    protected AbstractCommandResult(T context) {
        this.context = context;
    }

    public T getContext() {
        return context;
    }
}
