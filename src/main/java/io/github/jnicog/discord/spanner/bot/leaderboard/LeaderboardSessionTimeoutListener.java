package io.github.jnicog.discord.spanner.bot.leaderboard;

import io.github.jnicog.discord.spanner.bot.event.leaderboard.LeaderboardSessionTimeoutEvent;
import io.github.jnicog.discord.spanner.bot.notification.MessageGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listener for leaderboard session timeout events.
 * Clears reactions from timed-out leaderboard messages.
 */
@Component
public class LeaderboardSessionTimeoutListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeaderboardSessionTimeoutListener.class);

    private final MessageGateway messageGateway;

    public LeaderboardSessionTimeoutListener(MessageGateway messageGateway) {
        this.messageGateway = messageGateway;
    }

    @EventListener
    public void onSessionTimeout(LeaderboardSessionTimeoutEvent event) {
        LeaderboardSession session = event.session();
        LOGGER.debug("Handling timeout for leaderboard session {}", session.getMessageId());

        messageGateway.clearReactions(session.getChannelId(), session.getMessageId())
                .whenComplete((result, error) -> {
                    if (error != null) {
                        LOGGER.warn("Failed to clear reactions from timed-out leaderboard {}: {}",
                                session.getMessageId(), error.getMessage());
                    } else {
                        LOGGER.debug("Cleared reactions from timed-out leaderboard {}", session.getMessageId());
                    }
                });
    }
}

