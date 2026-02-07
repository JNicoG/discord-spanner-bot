package io.github.jnicog.discord.spanner.bot.command.dispatcher;

import io.github.jnicog.discord.spanner.bot.leaderboard.LeaderboardPage;
import io.github.jnicog.discord.spanner.bot.leaderboard.LeaderboardService;
import io.github.jnicog.discord.spanner.bot.leaderboard.LeaderboardSession;
import io.github.jnicog.discord.spanner.bot.leaderboard.LeaderboardSessionManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Optional;

/**
 * Dispatcher for handling reaction events on leaderboard messages.
 * Handles pagination via reaction emojis.
 */
@Component
public class LeaderboardReactionDispatcher extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeaderboardReactionDispatcher.class);

    private final LeaderboardSessionManager sessionManager;
    private final LeaderboardService leaderboardService;
    private final JdaResponseTranslator responseTranslator;
    private final JDA jda;

    public LeaderboardReactionDispatcher(LeaderboardSessionManager sessionManager,
                                          LeaderboardService leaderboardService,
                                          JdaResponseTranslator responseTranslator,
                                          JDA jda) {
        this.sessionManager = sessionManager;
        this.leaderboardService = leaderboardService;
        this.responseTranslator = responseTranslator;
        this.jda = jda;
    }

    @PostConstruct
    public void init() {
        // Set up the timeout callback to remove reactions when session expires
        sessionManager.setTimeoutCallback(this::handleSessionTimeout);
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        // Ignore bot reactions
        if (event.getUser() == null || event.getUser().isBot()) {
            return;
        }

        long messageId = event.getMessageIdLong();
        long userId = event.getUserIdLong();

        // Check if this is a leaderboard message we're tracking
        Optional<LeaderboardSession> sessionOpt = sessionManager.getSession(messageId);
        if (sessionOpt.isEmpty()) {
            return;
        }

        LeaderboardSession session = sessionOpt.get();

        // Check if user is authorised (only owner can interact)
        if (!session.isOwner(userId)) {
            // Silently ignore - do not respond to unauthorised users
            LOGGER.debug("Unauthorised user {} attempted to interact with leaderboard {}", userId, messageId);
            return;
        }

        String emoji = event.getReaction().getEmoji().getAsReactionCode();
        LOGGER.debug("Leaderboard reaction: {} on message {} by user {}", emoji, messageId, userId);

        // Remove the user's reaction to acknowledge the interaction
        event.getReaction().removeReaction(event.getUser()).queue(
            __ -> {},
            error -> LOGGER.warn("Failed to remove reaction: {}", error.getMessage())
        );

        // Handle pagination
        handlePagination(event, session, emoji);
    }

    private void handlePagination(MessageReactionAddEvent event, LeaderboardSession session, String emoji) {
        int currentPage = session.getCurrentPage();
        int newPage = currentPage;

        switch (emoji) {
            case JdaResponseTranslator.FIRST_PAGE -> newPage = 1;
            case JdaResponseTranslator.PREVIOUS_PAGE -> newPage = Math.max(1, currentPage - 1);
            case JdaResponseTranslator.NEXT_PAGE -> newPage = currentPage + 1;
            case JdaResponseTranslator.LAST_PAGE -> {
                LeaderboardPage lastPage = leaderboardService.getLastPage(session.getChannelId());
                newPage = lastPage.totalPages();
            }
            default -> {
                LOGGER.debug("Unknown reaction emoji: {}", emoji);
                return;
            }
        }

        // Get the new page
        LeaderboardPage page = leaderboardService.getLeaderboardPage(session.getChannelId(), newPage);

        // Update session with new page
        session.setCurrentPage(page.currentPage());

        // Reset the timeout since user interacted
        sessionManager.resetTimeout(session.getMessageId());

        // Update the message with new embed
        MessageEmbed newEmbed = responseTranslator.buildLeaderboardEmbed(page);
        event.getChannel().editMessageEmbedsById(session.getMessageId(), newEmbed).queue(
            __ -> LOGGER.debug("Updated leaderboard to page {}", page.currentPage()),
            error -> LOGGER.error("Failed to update leaderboard: {}", error.getMessage())
        );
    }

    private void handleSessionTimeout(LeaderboardSession session) {
        LOGGER.debug("Handling timeout for leaderboard session {}", session.getMessageId());

        // Remove all reactions from the message
        MessageChannel channel = jda.getChannelById(MessageChannel.class, session.getChannelId());
        if (channel != null) {
            channel.retrieveMessageById(session.getMessageId()).queue(
                message -> message.clearReactions().queue(
                    __ -> LOGGER.debug("Cleared reactions from timed-out leaderboard {}", session.getMessageId()),
                    error -> LOGGER.warn("Failed to clear reactions: {}", error.getMessage())
                ),
                error -> LOGGER.warn("Failed to retrieve message for timeout cleanup: {}", error.getMessage())
            );
        }
    }
}


