package io.github.jnicog.discord.spanner.bot.notification.handler;

import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

public interface CommandEventNotificationHandler<T extends AbstractCommandResult> {
    Class<T> handledEventType();
//    void handle(AbstractCommandResult event) throws Exception;
    void handle(T event) throws Exception;
    default void handleSafely(AbstractCommandResult event) throws Exception {
        handle(handledEventType().cast(event));
    }
}
