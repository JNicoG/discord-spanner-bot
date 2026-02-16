package io.github.jnicog.discord.spanner.bot.event.listener;

import io.github.jnicog.discord.spanner.bot.event.checkin.CheckInTimeoutEvent;
import io.github.jnicog.discord.spanner.bot.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Listener that handles queue management when a check-in session times out.
 *
 * <p>When a check-in timeout occurs, users who did NOT check in are removed
 * from the queue, while users who successfully checked in remain in the queue.</p>
 */
@Component
public class CheckInTimeoutQueueEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckInTimeoutQueueEventListener.class);

    private final QueueService queueService;

    public CheckInTimeoutQueueEventListener(QueueService queueService) {
        this.queueService = queueService;
    }

    /**
     * Removes users who did not check in from the queue after a timeout.
     * Users who checked in remain in the queue.
     */
    @EventListener
    @Order(2) // Run after timeout scheduling is handled
    public void onCheckInTimeout(CheckInTimeoutEvent event) {
        long channelId = event.getChannelId();
        Set<Long> usersWhoCheckedIn = event.getUsersWhoCheckedIn();
        Set<Long> usersWhoDidNotCheckIn = event.getUsersWhoDidNotCheckIn();

        LOGGER.info("Check-in timeout for channel {}: {} users checked in, {} did not",
                channelId, usersWhoCheckedIn.size(), usersWhoDidNotCheckIn.size());

        // Remove only users who did not check in from the queue
        for (Long userId : usersWhoDidNotCheckIn) {
            queueService.leaveQueue(userId, channelId);
            LOGGER.debug("Removed user {} from queue in channel {} (did not check in)", userId, channelId);
        }

        LOGGER.info("Removed {} users from queue in channel {} after check-in timeout (users who did not check in). " +
                        "{} users remain in queue.",
                usersWhoDidNotCheckIn.size(), channelId, usersWhoCheckedIn.size());
    }
}

