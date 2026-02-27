package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolver;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManDateFullEvent;
import io.github.jnicog.discord.spanner.bot.notification.handler.tenman.TenManCreatedNotificationHandler;
import io.github.jnicog.discord.spanner.bot.tenman.TenManPollSnapshot;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
public class TenManDateFullResponseResolver implements ResponseResolver<TenManDateFullEvent> {

    private static final DateTimeFormatter BUTTON_FORMAT = DateTimeFormatter.ofPattern("EEE d MMM", Locale.ENGLISH);

    @Override
    public InteractionResponse resolve(TenManDateFullEvent event) {
        TenManPollSnapshot snapshot = event.getUpdatedSnapshot();
        String pollContent = TenManCreatedNotificationHandler.formatPollMessage(
                snapshot.dateOptions(), snapshot.timeDisplay(), snapshot.testMode(), snapshot.closesAt());
        String content = "🔒  Poll closed — 10-Man found!\n\n" + pollContent;

        Long lockedId = snapshot.lockedDateOptionId();
        List<InteractionResponse.ButtonSpec> buttonSpecs = snapshot.dateOptions().stream()
                .map(opt -> new InteractionResponse.ButtonSpec(
                        "tenman_" + opt.id(),
                        opt.date().format(BUTTON_FORMAT),
                        lockedId == null || opt.id() != lockedId
                ))
                .toList();

        return new InteractionResponse.EditButtonMessageWithComponents(content, buttonSpecs);
    }
}
