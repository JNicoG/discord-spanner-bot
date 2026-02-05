package io.github.jnicog.discord.spanner.bot.notification.handler;

import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

public interface CommandEventNotificationHandler<T extends AbstractCommandResult> {
    void handle(T event) throws Exception;

    @SuppressWarnings("unchecked")
    default void handleSafely(AbstractCommandResult event) throws Exception {
        handle((T) event);
    }
}
