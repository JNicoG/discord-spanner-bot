package io.github.jnicog.discord.spanner.bot.notification;

import com.google.common.collect.ImmutableMap;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import io.github.jnicog.discord.spanner.bot.notification.handler.CommandEventNotificationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service consumes AbstractCommandResults and sends notifications as needed.
 */
@Service
public class NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);

    private final ImmutableMap<Class<? extends AbstractCommandResult>, CommandEventNotificationHandler<? extends AbstractCommandResult>> EVENT_HANDLER_MAP;

    public NotificationService(List<CommandEventNotificationHandler<? extends AbstractCommandResult>> eventNotificationHandlers) {
        ImmutableMap.Builder<Class<? extends AbstractCommandResult>, CommandEventNotificationHandler<? extends AbstractCommandResult>> builder = ImmutableMap.builder();
        for (CommandEventNotificationHandler<? extends AbstractCommandResult> handler : eventNotificationHandlers) {
            builder.put(handler.handledEventType(), handler);
        }
        EVENT_HANDLER_MAP = builder.build();
//        EVENT_HANDLER_MAP = eventNotificationHandlers.stream()
//                .flatMap(handler -> handler.handledEventType().stream()
//                        .map(eventType -> Map.entry(eventType, handler)))
//                .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @EventListener
    public void onCommandResultEvent(AbstractCommandResult event) {
        LOGGER.debug("Consuming AbstractCommandResult event: {}", event.getClass());
        if (event.getInteractionResponder() == null) {
            LOGGER.warn("No responder found for command event: {}", event.getCommandName());
            return;
        }

        LOGGER.debug("Resolving notification handler for event type: {}", event.getClass());
        LOGGER.debug("Available handlers: {}", EVENT_HANDLER_MAP.get(event.getClass()));

//        CommandEventNotificationHandler<? extends AbstractCommandResult> handler = EVENT_HANDLER_MAP.get(event.getClass());
//        if (handler == null) {
//            LOGGER.error("No notification handler found for event type: {}", event.getClass());
//            return;
//        }
//        handler.handle(handler.handledEventType().cast(event));
        CommandEventNotificationHandler<? extends AbstractCommandResult> handler = resolveHandlerFor(event);
        if (handler == null) {
            LOGGER.error("No notification handler found for event type: {}", event.getClass());
            return;
        }

        try {
            handler.handleSafely(event);
        } catch (Exception e) {
            LOGGER.error("Error handling notification for event type: {}", event.getClass(), e);
        }

    }

    private CommandEventNotificationHandler<? extends AbstractCommandResult> resolveHandlerFor(AbstractCommandResult event) {
        // exact match first
        CommandEventNotificationHandler<? extends AbstractCommandResult> handler = EVENT_HANDLER_MAP.get(event.getClass());
        if (handler != null) return handler;

        // fallback: find first registered key that is assignable from the event's class
        for (Class<? extends AbstractCommandResult> key : EVENT_HANDLER_MAP.keySet()) {
            if (key.isInstance(event)) {
                return EVENT_HANDLER_MAP.get(key);
            }
        }
        return null;
    }
}
