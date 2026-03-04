package io.github.jnicog.discord.spanner.bot.notification.handler.tenman;

import io.github.jnicog.discord.spanner.bot.event.tenman.TenManRosterResignedButtonEvent;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManRosterResignedSlashEvent;
import io.github.jnicog.discord.spanner.bot.notification.TenManMessageGateway;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
public class TenManRosterResignedNotificationHandler {

    private static final DateTimeFormatter LONG_FORMAT = DateTimeFormatter.ofPattern("EEEE d MMM", Locale.ENGLISH);

    private final TenManMessageGateway gateway;

    public TenManRosterResignedNotificationHandler(TenManMessageGateway gateway) {
        this.gateway = gateway;
    }

    @EventListener
    public void onResignedByButton(TenManRosterResignedButtonEvent event) {
        sendAnnouncement(event.getChannelId(), event.getUserId(), event.getLockedDate(),
                event.getUpdatedSnapshot().timeDisplay(), event.getSlotsNeeded());
    }

    @EventListener
    public void onResignedBySlash(TenManRosterResignedSlashEvent event) {
        sendAnnouncement(event.getChannelId(), event.getUserId(), event.getLockedDate(),
                event.getTimeDisplay(), event.getSlotsNeeded());
    }

    private void sendAnnouncement(long channelId, long userId, LocalDate date, String timeDisplay, int slotsNeeded) {
        String dateLabel = date.format(LONG_FORMAT);
        String timeClause = (timeDisplay != null && !timeDisplay.isBlank()) ? " at " + timeDisplay : "";
        String msg = "<@" + userId + "> has signed off the ten man roster. +1 spanner 🔧\n\n"
                + "Looking for " + slotsNeeded + " to fill in for ten mans on " + dateLabel + timeClause
                + ". Use `/ten-man-fill` to take a slot.";
        gateway.sendNotification(channelId, msg);
    }
}
