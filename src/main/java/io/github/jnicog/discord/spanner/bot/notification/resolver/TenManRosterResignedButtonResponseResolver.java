package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolver;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManRosterResignedButtonEvent;
import io.github.jnicog.discord.spanner.bot.notification.handler.tenman.TenManCreatedNotificationHandler;
import org.springframework.stereotype.Component;

@Component
public class TenManRosterResignedButtonResponseResolver implements ResponseResolver<TenManRosterResignedButtonEvent> {

    @Override
    public InteractionResponse resolve(TenManRosterResignedButtonEvent event) {
        int n = event.getSlotsNeeded();
        String header = "⚠️  Looking for " + n + " replacement" + (n == 1 ? "" : "s") + "!\n\n";
        String pollContent = TenManCreatedNotificationHandler.formatPollMessage(
                event.getUpdatedSnapshot().dateOptions(),
                event.getUpdatedSnapshot().timeDisplay(),
                event.getUpdatedSnapshot().testMode(),
                event.getUpdatedSnapshot().closesAt());
        return new InteractionResponse.EditButtonMessage(header + pollContent);
    }
}
