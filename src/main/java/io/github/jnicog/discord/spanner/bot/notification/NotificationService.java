package io.github.jnicog.discord.spanner.bot.notification;

import com.google.common.collect.ImmutableMap;
import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.PendingInteraction;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolver;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.GenericTypeResolver;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *  notification service that works with PendingInteraction.
 */
@Service
public class NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);

    private final ImmutableMap<Class<? extends AbstractCommandResult<?>>, ResponseResolver<? extends AbstractCommandResult<?>>> resolverMap;

    @SuppressWarnings("unchecked")
    public NotificationService(List<ResponseResolver<? extends AbstractCommandResult<?>>> resolvers) {
        ImmutableMap.Builder<Class<? extends AbstractCommandResult<?>>, ResponseResolver<? extends AbstractCommandResult<?>>> builder = ImmutableMap.builder();

        for (ResponseResolver<?> resolver : resolvers) {
            Class<?> eventType = GenericTypeResolver.resolveTypeArgument(resolver.getClass(), ResponseResolver.class);

            if (eventType != null && AbstractCommandResult.class.isAssignableFrom(eventType)) {
                builder.put((Class<? extends AbstractCommandResult<?>>) eventType, resolver);
            } else {
                LOGGER.warn("Could not resolve event type for resolver: {}", resolver.getClass());
            }
        }
        resolverMap = builder.build();
    }

    @EventListener
    public void onPendingInteraction(PendingInteraction<?> pending) {
        AbstractCommandResult<?> result = pending.getResult();
        LOGGER.debug("Processing pending interaction for: {}", result.getClass());

        ResponseResolver<AbstractCommandResult<?>> resolver = findResolver(result);
        if (resolver == null) {
            LOGGER.error("No response resolver found for: {}", result.getClass());
            pending.respond(new InteractionResponse.EphemeralReply("An error occurred."));
            return;
        }

        try {
            InteractionResponse response = resolver.resolve(result);
            pending.respond(response);
        } catch (Exception e) {
            LOGGER.error("Error resolving response for: {}", result.getClass(), e);
            pending.respond(new InteractionResponse.EphemeralReply("An error occurred."));
        }
    }

    @SuppressWarnings("unchecked")
    private ResponseResolver<AbstractCommandResult<?>> findResolver(AbstractCommandResult<?> result) {
        // Exact match first
        ResponseResolver<?> resolver = resolverMap.get(result.getClass());
        if (resolver != null) {
            return (ResponseResolver<AbstractCommandResult<?>>) resolver;
        }

        // Fallback: find assignable
        for (var entry : resolverMap.entrySet()) {
            if (entry.getKey().isInstance(result)) {
                return (ResponseResolver<AbstractCommandResult<?>>) entry.getValue();
            }
        }
        return null;
    }
}
