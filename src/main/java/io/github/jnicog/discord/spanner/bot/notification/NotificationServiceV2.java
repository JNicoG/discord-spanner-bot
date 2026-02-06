package io.github.jnicog.discord.spanner.bot.notification;

import com.google.common.collect.ImmutableMap;
import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.PendingInteractionV2;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolverV2;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResultV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.GenericTypeResolver;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * V2 notification service that works with PendingInteractionV2.
 */
@Service
public class NotificationServiceV2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceV2.class);

    private final ImmutableMap<Class<? extends AbstractCommandResultV2<?>>, ResponseResolverV2<? extends AbstractCommandResultV2<?>>> resolverMap;

    @SuppressWarnings("unchecked")
    public NotificationServiceV2(List<ResponseResolverV2<? extends AbstractCommandResultV2<?>>> resolvers) {
        ImmutableMap.Builder<Class<? extends AbstractCommandResultV2<?>>, ResponseResolverV2<? extends AbstractCommandResultV2<?>>> builder = ImmutableMap.builder();

        for (ResponseResolverV2<?> resolver : resolvers) {
            Class<?> eventType = GenericTypeResolver.resolveTypeArgument(resolver.getClass(), ResponseResolverV2.class);

            if (eventType != null && AbstractCommandResultV2.class.isAssignableFrom(eventType)) {
                builder.put((Class<? extends AbstractCommandResultV2<?>>) eventType, resolver);
            } else {
                LOGGER.warn("Could not resolve event type for resolver: {}", resolver.getClass());
            }
        }
        resolverMap = builder.build();
    }

    @EventListener
    public void onPendingInteraction(PendingInteractionV2<?> pending) {
        AbstractCommandResultV2<?> result = pending.getResult();
        LOGGER.debug("Processing pending interaction for: {}", result.getClass());

        ResponseResolverV2<AbstractCommandResultV2<?>> resolver = findResolver(result);
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
    private ResponseResolverV2<AbstractCommandResultV2<?>> findResolver(AbstractCommandResultV2<?> result) {
        // Exact match first
        ResponseResolverV2<?> resolver = resolverMap.get(result.getClass());
        if (resolver != null) {
            return (ResponseResolverV2<AbstractCommandResultV2<?>>) resolver;
        }

        // Fallback: find assignable
        for (var entry : resolverMap.entrySet()) {
            if (entry.getKey().isInstance(result)) {
                return (ResponseResolverV2<AbstractCommandResultV2<?>>) entry.getValue();
            }
        }
        return null;
    }
}
