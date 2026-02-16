package io.github.jnicog.discord.spanner.bot.command.dispatcher;

import io.github.jnicog.discord.spanner.bot.event.leaderboard.LeaderboardPaginationEvent;
import io.github.jnicog.discord.spanner.bot.leaderboard.LeaderboardPaginationAction;
import io.github.jnicog.discord.spanner.bot.leaderboard.LeaderboardSession;
import io.github.jnicog.discord.spanner.bot.leaderboard.LeaderboardSessionManager;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * Dispatcher for handling reaction events on leaderboard messages.
 * Publishes events for handlers to process pagination.
 *
 * <p>This dispatcher follows the same pattern as {@link ButtonDispatcher} and
 * {@link SlashCommandDispatcher}</p>
 */
@Component
public class LeaderboardReactionDispatcher extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeaderboardReactionDispatcher.class);

    private static final Map<String, LeaderboardPaginationAction> EMOJI_TO_ACTION = Map.of(
            JdaResponseTranslator.FIRST_PAGE, LeaderboardPaginationAction.FIRST_PAGE,
            JdaResponseTranslator.PREVIOUS_PAGE, LeaderboardPaginationAction.PREVIOUS_PAGE,
            JdaResponseTranslator.NEXT_PAGE, LeaderboardPaginationAction.NEXT_PAGE,
            JdaResponseTranslator.LAST_PAGE, LeaderboardPaginationAction.LAST_PAGE
    );

    private final LeaderboardSessionManager sessionManager;
    private final ApplicationEventPublisher eventPublisher;

    public LeaderboardReactionDispatcher(LeaderboardSessionManager sessionManager,
                                          ApplicationEventPublisher eventPublisher) {
        this.sessionManager = sessionManager;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (event.getUser() == null || event.getUser().isBot()) {
            return;
        }

        long messageId = event.getMessageIdLong();
        long userId = event.getUserIdLong();

        Optional<LeaderboardSession> sessionOpt = sessionManager.getSession(messageId);
        if (sessionOpt.isEmpty()) {
            return;
        }

        LeaderboardSession session = sessionOpt.get();
        String emoji = event.getReaction().getEmoji().getAsReactionCode();

        // Convert emoji to action - ignore unrecognised emojis
        LeaderboardPaginationAction action = EMOJI_TO_ACTION.get(emoji);
        if (action == null) {
            LOGGER.debug("Ignoring unrecognised emoji {} on leaderboard {}", emoji, messageId);
            return;
        }

        LOGGER.debug("Leaderboard reaction: {} ({}) on message {} by user {}",
                action, emoji, messageId, userId);

        // Remove the user's reaction to acknowledge the interaction
        event.getReaction().removeReaction(event.getUser()).queue(
            success -> {},
            error -> LOGGER.warn("Failed to remove reaction: {}", error.getMessage())
        );

        if (!session.isOwner(userId)) {
            LOGGER.debug("Unauthorised user {} attempted to interact with leaderboard {}", userId, messageId);
            return;
        }

        eventPublisher.publishEvent(new LeaderboardPaginationEvent(
                event.getChannel().getIdLong(),
                messageId,
                session,
                action
        ));
    }
}


