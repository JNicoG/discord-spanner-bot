package io.github.jnicog.discord.spanner.bot.notification.handler;

import io.github.jnicog.discord.spanner.bot.command.dispatcher.JdaResponseTranslator;
import io.github.jnicog.discord.spanner.bot.event.leaderboard.LeaderboardPageUpdateEvent;
import io.github.jnicog.discord.spanner.bot.notification.MessageGateway;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Handles leaderboard page update events by editing the message with the new embed.
 */
@Component
public class LeaderboardPageUpdateNotificationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeaderboardPageUpdateNotificationHandler.class);

    private final JdaResponseTranslator responseTranslator;
    private final MessageGateway messageGateway;

    public LeaderboardPageUpdateNotificationHandler(JdaResponseTranslator responseTranslator,
                                                     MessageGateway messageGateway) {
        this.responseTranslator = responseTranslator;
        this.messageGateway = messageGateway;
    }

    @EventListener
    public void handlePageUpdate(LeaderboardPageUpdateEvent event) {
        MessageEmbed newEmbed = responseTranslator.buildLeaderboardEmbed(event.page());

        messageGateway.editMessageEmbed(event.channelId(), event.messageId(), newEmbed)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        LOGGER.error("Failed to update leaderboard message {}: {}",
                                event.messageId(), error.getMessage());
                    } else {
                        LOGGER.debug("Updated leaderboard to page {}", event.page().currentPage());
                    }
                });
    }
}

