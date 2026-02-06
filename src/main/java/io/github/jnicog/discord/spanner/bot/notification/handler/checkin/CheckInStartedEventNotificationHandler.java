package io.github.jnicog.discord.spanner.bot.notification.handler.checkin;

import io.github.jnicog.discord.spanner.bot.checkin.CheckInService;
import io.github.jnicog.discord.spanner.bot.event.checkin.CheckInStartedEvent;
import io.github.jnicog.discord.spanner.bot.notification.CheckInMessageGateway;
import io.github.jnicog.discord.spanner.bot.notification.MessageFormatterService;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CheckInStartedEventNotificationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckInStartedEventNotificationHandler.class);

    private final CheckInMessageGateway checkInMessageGateway;
    private final CheckInService checkInService;
    private final MessageFormatterService messageFormatter;

    private static final Emoji CHECK_MARK_EMOJI = Emoji.fromUnicode("U+2714");
    public static final Emoji SPANNER_EMOJI = Emoji.fromUnicode("U+1F527");

    public static final Button ACCEPT_BUTTON = Button.success("checkInAccept", CHECK_MARK_EMOJI);
    public static final Button SPANNER_BUTTON = Button.danger("checkInSpanner", SPANNER_EMOJI);

    public CheckInStartedEventNotificationHandler(CheckInMessageGateway checkInMessageGateway,
                                                  CheckInService checkInService,
                                                  MessageFormatterService messageFormatter) {
        this.checkInMessageGateway = checkInMessageGateway;
        this.checkInService = checkInService;
        this.messageFormatter = messageFormatter;
    }

    @EventListener
    public void onStart(CheckInStartedEvent event) {
        Map<Long, Boolean> checkInStatusSnapshot = event.getSession().getUserCheckInStatusSnapshot();
        String message = messageFormatter.formatCheckInStarted(checkInStatusSnapshot);

        checkInMessageGateway.sendCheckInStartMessage(event.getSession().getChannelId(), message)
                .thenApply(Long::parseLong)
                .thenCompose(messageId -> {
                    checkInService.registerMessageId(event.getSession().getChannelId(), messageId);
                    return checkInMessageGateway.attachCheckInButtons(event.getSession().getChannelId(), messageId);
                })
                .exceptionally(ex -> {
                    LOGGER.error("Failed to send check-in start message for channelId: {}",
                            event.getSession().getChannelId(), ex);
                    throw new RuntimeException("Failed to send check-in start message for channelId: "
                            + event.getSession().getChannelId(), ex);
                });
    }
}
