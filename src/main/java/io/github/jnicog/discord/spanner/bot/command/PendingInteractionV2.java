package io.github.jnicog.discord.spanner.bot.command;

import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResultV2;

/**
 * V2 version of PendingInteraction that works with AbstractCommandResultV2.
 * Holds a command result along with a callback to send the response.
 * This allows the notification layer to resolve responses without knowing about JDA.
 *
 * <p>The dispatcher creates this wrapper and the notification service calls
 * {@link #respond(InteractionResponse)} which internally delegates to the JDA translator.</p>
 *
 * @param <T> The type of the command result
 */
public class PendingInteractionV2<T extends AbstractCommandResultV2<?>> {

    private final T result;
    private final ResponseCallback responseCallback;

    public PendingInteractionV2(T result, ResponseCallback responseCallback) {
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

