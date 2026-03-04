package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolver;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManStatusEvent;
import io.github.jnicog.discord.spanner.bot.notification.handler.tenman.TenManCreatedNotificationHandler;
import io.github.jnicog.discord.spanner.bot.tenman.TenManPollSnapshot;
import org.springframework.stereotype.Component;

@Component
public class TenManStatusResponseResolver implements ResponseResolver<TenManStatusEvent> {

    @Override
    public InteractionResponse resolve(TenManStatusEvent event) {
        TenManPollSnapshot snapshot = event.getSnapshot();
        String content = TenManCreatedNotificationHandler.formatPollMessage(
                snapshot.dateOptions(), snapshot.timeDisplay(), snapshot.testMode(), snapshot.closesAt());
        if (snapshot.lockedDateOptionId() != null) {
            content = "🔒  Ten-man roster is locked in!\n\n" + content;
        }
        return new InteractionResponse.EphemeralReply(content);
    }
}
