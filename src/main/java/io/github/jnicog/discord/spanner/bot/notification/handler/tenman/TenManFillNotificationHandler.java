package io.github.jnicog.discord.spanner.bot.notification.handler.tenman;

import io.github.jnicog.discord.spanner.bot.event.tenman.TenManFillSuccessEvent;
import io.github.jnicog.discord.spanner.bot.notification.TenManMessageGateway;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class TenManFillNotificationHandler {

    private static final DateTimeFormatter LONG_FORMAT = DateTimeFormatter.ofPattern("EEEE d MMM", Locale.ENGLISH);

    private final TenManMessageGateway gateway;

    public TenManFillNotificationHandler(TenManMessageGateway gateway) {
        this.gateway = gateway;
    }

    @EventListener
    public void onFillSuccess(TenManFillSuccessEvent event) {
        String dateLabel = event.getDate().format(LONG_FORMAT);
        String timeClause = (event.getTimeDisplay() != null && !event.getTimeDisplay().isBlank())
                ? " at " + event.getTimeDisplay() : "";

        if (event.isRosterRestored()) {
            String mentions = event.getRosterUserIds().stream()
                    .map(id -> "<@" + id + ">")
                    .collect(Collectors.joining(" "));
            gateway.sendNotification(event.getChannelId(),
                    "🎮  Roster complete! Ten-man is back on for " + dateLabel + timeClause + "!\n" + mentions);
        } else {
            gateway.sendNotification(event.getChannelId(),
                    "A slot for ten mans on " + dateLabel + timeClause + " has been filled by <@" + event.getUserId() + ">! "
                    + "Still looking for " + event.getSlotsNeeded() + " more. Use `/ten-man-fill` to take a slot.");
        }
    }
}
