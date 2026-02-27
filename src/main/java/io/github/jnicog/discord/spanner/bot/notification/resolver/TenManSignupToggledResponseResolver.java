package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolver;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManDateFullEvent;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManSignupToggledEvent;
import io.github.jnicog.discord.spanner.bot.notification.handler.tenman.TenManCreatedNotificationHandler;
import org.springframework.stereotype.Component;

@Component
public class TenManSignupToggledResponseResolver implements ResponseResolver<TenManSignupToggledEvent> {

    @Override
    public InteractionResponse resolve(TenManSignupToggledEvent event) {
        String content = TenManCreatedNotificationHandler.formatPollMessage(
                event.getUpdatedSnapshot().dateOptions(), event.getUpdatedSnapshot().timeDisplay(), event.getUpdatedSnapshot().testMode(), event.getUpdatedSnapshot().closesAt());
        return new InteractionResponse.EditButtonMessage(content);
    }
}
