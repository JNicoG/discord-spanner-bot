package io.github.jnicog.discord.spanner.bot.leaderboard;

import io.github.jnicog.discord.spanner.bot.event.leaderboard.LeaderboardPageUpdateEvent;
import io.github.jnicog.discord.spanner.bot.event.leaderboard.LeaderboardPaginationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Handles leaderboard pagination events.
 * Processes pagination requests and publishes page update events.
 */
@Component
public class LeaderboardEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeaderboardEventHandler.class);

    private final LeaderboardSessionManager sessionManager;
    private final LeaderboardService leaderboardService;
    private final ApplicationEventPublisher eventPublisher;

    public LeaderboardEventHandler(LeaderboardSessionManager sessionManager,
                                    LeaderboardService leaderboardService,
                                    ApplicationEventPublisher eventPublisher) {
        this.sessionManager = sessionManager;
        this.leaderboardService = leaderboardService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Handles pagination events from leaderboard reactions.
     */
    @EventListener
    public void handlePagination(LeaderboardPaginationEvent event) {
        LeaderboardSession session = event.session();
        LeaderboardPaginationAction action = event.action();
        int currentPage = session.getCurrentPage();

        int newPage = switch (action) {
            case FIRST_PAGE -> 1;
            case PREVIOUS_PAGE -> Math.max(1, currentPage - 1);
            case NEXT_PAGE -> currentPage + 1;
            case LAST_PAGE -> leaderboardService.getLastPage(session.getChannelId()).totalPages();
        };

        LeaderboardPage page = leaderboardService.getLeaderboardPage(session.getChannelId(), newPage);
        session.setCurrentPage(page.currentPage());
        sessionManager.resetTimeout(session.getMessageId());

        eventPublisher.publishEvent(new LeaderboardPageUpdateEvent(
                event.channelId(),
                event.messageId(),
                page
        ));

        LOGGER.debug("Processed {} to page {} for leaderboard {}",
                action, page.currentPage(), session.getMessageId());
    }
}



