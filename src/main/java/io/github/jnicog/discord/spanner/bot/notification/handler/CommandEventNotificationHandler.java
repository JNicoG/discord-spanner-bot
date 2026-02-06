package io.github.jnicog.discord.spanner.bot.notification.handler;

import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

/**
 * V1 notification handler interface.
 *
 * @deprecated Use {@link io.github.jnicog.discord.spanner.bot.command.ResponseResolverV2} instead.
 */
@Deprecated
public interface CommandEventNotificationHandler<T extends AbstractCommandResult> {
    void handle(T event) throws Exception;

    @SuppressWarnings("unchecked")
    default void handleSafely(AbstractCommandResult event) throws Exception {
        handle((T) event);
    }
}
