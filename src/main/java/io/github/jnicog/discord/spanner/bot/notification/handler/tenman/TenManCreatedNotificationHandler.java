package io.github.jnicog.discord.spanner.bot.notification.handler.tenman;

import io.github.jnicog.discord.spanner.bot.event.tenman.TenManPollCreatedEvent;
import io.github.jnicog.discord.spanner.bot.tenman.TenManDateOptionSnapshot;
import io.github.jnicog.discord.spanner.bot.tenman.TenManService;
import io.github.jnicog.discord.spanner.bot.notification.TenManMessageGateway;
import net.dv8tion.jda.api.components.buttons.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class TenManCreatedNotificationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TenManCreatedNotificationHandler.class);
    private static final DateTimeFormatter BUTTON_FORMAT = DateTimeFormatter.ofPattern("EEE d MMM", Locale.ENGLISH);
    private static final DateTimeFormatter LONG_FORMAT = DateTimeFormatter.ofPattern("EEEE d MMM", Locale.ENGLISH);
    private static final int CAPACITY = 10;

    private final TenManMessageGateway gateway;
    private final TenManService tenManService;

    public TenManCreatedNotificationHandler(TenManMessageGateway gateway, TenManService tenManService) {
        this.gateway = gateway;
        this.tenManService = tenManService;
    }

    @EventListener
    public void onPollCreated(TenManPollCreatedEvent event) {
        String content = formatPollMessage(event.getDateOptions(), event.getTimeDisplay(), event.isTestMode(), event.getClosesAt());

        List<Button> buttons = event.getDateOptions().stream()
                .map(opt -> Button.primary(
                        "tenman_" + opt.id(),
                        opt.date().format(BUTTON_FORMAT)
                ))
                .collect(Collectors.toList());

        gateway.sendPollMessage(event.getChannelId(), content, buttons)
                .thenAccept(messageId -> tenManService.registerMessageId(event.getPollId(), messageId))
                .exceptionally(ex -> {
                    LOGGER.error("Failed to send ten-man poll message for channelId: {}", event.getChannelId(), ex);
                    return null;
                });
    }

    public static String formatPollMessage(List<TenManDateOptionSnapshot> dateOptions, String timeDisplay, boolean testMode, OffsetDateTime closesAt) {
        int capacity = testMode ? 1 : CAPACITY;
        StringBuilder sb = new StringBuilder();
        sb.append("📅  10-Man Sign-up");
        if (testMode) {
            sb.append("  *(test mode)*");
        }
        sb.append("\n");
        if (timeDisplay != null && !timeDisplay.isBlank()) {
            sb.append("🕐  ").append(timeDisplay).append("\n");
        }
        if (closesAt != null) {
            sb.append("⏰  Closes <t:").append(closesAt.toEpochSecond()).append(":R>\n");
        }
        sb.append("──────────────────\n");
        for (TenManDateOptionSnapshot opt : dateOptions) {
            String dateLabel = opt.date().format(LONG_FORMAT);
            int count = opt.signedUpUserIds().size();
            String mentions = opt.signedUpUserIds().stream()
                    .map(id -> "<@" + id + ">")
                    .collect(Collectors.joining(", "));
            if (mentions.isEmpty()) {
                sb.append(String.format("%s  —  %d / %d%n", dateLabel, count, capacity));
            } else {
                sb.append(String.format("%s  —  %d / %d  (%s)%n", dateLabel, count, capacity, mentions));
            }
        }
        return sb.toString().stripTrailing();
    }
}
