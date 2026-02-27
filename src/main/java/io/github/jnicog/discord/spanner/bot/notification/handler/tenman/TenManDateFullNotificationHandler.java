package io.github.jnicog.discord.spanner.bot.notification.handler.tenman;

import io.github.jnicog.discord.spanner.bot.event.tenman.TenManDateFullEvent;
import io.github.jnicog.discord.spanner.bot.notification.TenManMessageGateway;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class TenManDateFullNotificationHandler {

    private static final DateTimeFormatter LONG_FORMAT = DateTimeFormatter.ofPattern("EEEE d MMM", Locale.ENGLISH);

    private final TenManMessageGateway gateway;

    public TenManDateFullNotificationHandler(TenManMessageGateway gateway) {
        this.gateway = gateway;
    }

    @EventListener
    public void onDateFull(TenManDateFullEvent event) {
        String dateLabel = event.getDate().format(LONG_FORMAT);
        String timeDisplay = event.getUpdatedSnapshot().timeDisplay();

        String heading;
        if (timeDisplay != null && !timeDisplay.isBlank()) {
            heading = "🎮  10-Man is on — " + dateLabel + " at " + timeDisplay + "!";
        } else {
            heading = "🎮  10-Man is on — " + dateLabel + "!";
        }

        String mentions = event.getSignedUpUserIds().stream()
                .map(id -> "<@" + id + ">")
                .collect(Collectors.joining(" "));

        gateway.sendNotification(event.getChannelId(), heading + "\n" + mentions);
    }
}
