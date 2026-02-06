package io.github.jnicog.discord.spanner.bot.command;

import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

/**
 * V1 pending interaction wrapper for V1 events.
 *
 * @param <T> The type of the command result
 * @deprecated Use {@link PendingInteractionV2} instead which works with V2 JDA-free events.
 */
@Deprecated
public class PendingInteraction<T extends AbstractCommandResult<?>> {

    private final T result;
    private final ResponseCallback responseCallback;

    public PendingInteraction(T result, ResponseCallback responseCallback) {
        this.result = result;
        this.responseCallback = responseCallback;
    }

    public T getResult() {
        return result;
    }

    /**
     * Send the response. This is the only way to respond - it's a push model
     * rather than passing a responder through the domain.
     */
    public void respond(InteractionResponse response) {
        responseCallback.send(response);
    }

    @FunctionalInterface
    public interface ResponseCallback {
        void send(InteractionResponse response);
    }
}
