package io.github.jnicog.discord.spanner.bot.event;

/**
 * Base class for  command results that use JDA-free context.
 * Unlike AbstractCommandResult, this doesn't require InteractionContext
 * which contains the JDA responder.
 *
 * @param <T> The type of context this result uses (e.g., SlashCommandContext, ButtonInteractionContext)
 */
public abstract class AbstractCommandResult<T> {

    private final T context;

    protected AbstractCommandResult(T context) {
        this.context = context;
    }

    public T getContext() {
        return context;
    }
}

