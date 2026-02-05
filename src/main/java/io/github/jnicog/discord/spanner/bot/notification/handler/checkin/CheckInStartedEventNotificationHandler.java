package io.github.jnicog.discord.spanner.bot.notification.handler.checkin;

import io.github.jnicog.discord.spanner.bot.checkin.CheckInService;
import io.github.jnicog.discord.spanner.bot.config.QueueProperties;
import io.github.jnicog.discord.spanner.bot.event.checkin.CheckInStartedEvent;
import io.github.jnicog.discord.spanner.bot.notification.CheckInMessageGateway;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CheckInStartedEventNotificationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckInStartedEventNotificationHandler.class);

    private final QueueProperties queueProperties;
    private final CheckInMessageGateway checkInMessageGateway;
    private final CheckInService checkInService;

    private static final Emoji CHECK_MARK_EMOJI = Emoji.fromUnicode("U+2714");

    private static final String CHECKED_IN_SYMBOL = CHECK_MARK_EMOJI.toString();
    private static final String NOT_CHECKED_IN_SYMBOL = "X";

    public static final Emoji SPANNER_EMOJI = Emoji.fromUnicode("U+1F527");

    public static final Button ACCEPT_BUTTON = Button.success("acceptButton", CHECK_MARK_EMOJI);
    public static final Button SPANNER_BUTTON = Button.danger("spannerButton", SPANNER_EMOJI);

    public CheckInStartedEventNotificationHandler(QueueProperties queueProperties,
                                                  CheckInMessageGateway checkInMessageGateway,
                                                  CheckInService checkInService) {
        this.queueProperties = queueProperties;
        this.checkInMessageGateway = checkInMessageGateway;
        this.checkInService = checkInService;
    }

    @EventListener
    public void onStart(CheckInStartedEvent event) {
        Map<Long, Boolean> checkInStatusSnapshot = event.getSession().getUserCheckInStatusSnapshot();
        String playerStatusList = checkInStatusSnapshot.entrySet().stream()
                .map(entry -> String.format("<@%d> [%s]", entry.getKey(), entry.getValue()
                        ? CHECKED_IN_SYMBOL : NOT_CHECKED_IN_SYMBOL))
                .collect(Collectors.joining(" | "));

        String message = String.format(
                "The queue has been filled!\n" +
                "Click the %s button within %s %s to accept.\n" +
                "Waiting for all players to accept...\n" +
                "%s",
                CHECK_MARK_EMOJI,
                queueProperties.getCheckInTimeoutLength(),
                queueProperties.getCheckInTimeoutLength() == 1 ?
                        StringUtils.chop(queueProperties.getCheckInTimeoutUnit().toString().toLowerCase())
                        : queueProperties.getCheckInTimeoutUnit().toString().toLowerCase(),
                playerStatusList
        );

        checkInMessageGateway.sendCheckInStartMessage(event.getSession().getChannelId(), message)
                .thenApply(Long::parseLong)
                .thenCompose(messageId -> {
                    checkInService.registerMessageId(event.getSession().getChannelId(), messageId);
                    return checkInMessageGateway.attachCheckInButtons(event.getSession().getChannelId(), messageId);
                })
                .exceptionally(ex -> {
                    LOGGER.error("Failed to send check-in start message for channelId: {}",
                            event.getSession().getChannelId(), ex);
                    // Perhaps cancel the session if the message fails to send since the session won't proceed
                    // checkInService.abortSession(channelId);
                    throw new RuntimeException("Failed to send check-in start message for channelId: "
                            + event.getSession().getChannelId(), ex);
                });
    }
}
