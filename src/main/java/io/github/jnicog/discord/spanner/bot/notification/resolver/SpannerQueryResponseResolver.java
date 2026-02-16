package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolver;
import io.github.jnicog.discord.spanner.bot.event.spanner.SpannerQueryEvent;
import io.github.jnicog.discord.spanner.bot.notification.MessageFormatterService;
import org.springframework.stereotype.Component;

/**
 * ResponseResolver for SpannerQueryEvent.
 */
@Component
public class SpannerQueryResponseResolver implements ResponseResolver<SpannerQueryEvent> {

    private final MessageFormatterService messageFormatter;

    public SpannerQueryResponseResolver(MessageFormatterService messageFormatter) {
        this.messageFormatter = messageFormatter;
    }

    @Override
    public InteractionResponse resolve(SpannerQueryEvent event) {
        String message;

        if (event.isSelfQuery()) {
            message = messageFormatter.formatSelfSpannerCount(event.getSpannerCount());
        } else {
            message = messageFormatter.formatUserSpannerCount(
                    event.getTargetUserId(),
                    event.getSpannerCount()
            );
        }

        return new InteractionResponse.PublicReply(message);
    }
}

