package io.github.jnicog.discord.spanner.bot.notification;

import io.github.jnicog.discord.spanner.bot.config.QueueProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Centralised service for formatting Discord messages.
 * Extracts message formatting logic from response resolvers.
 */
@Service
public class MessageFormatterService {

    private static final String CHECKED_IN_SYMBOL = "✔";
    private static final String NOT_CHECKED_IN_SYMBOL = "X";

    private final QueueProperties queueProperties;

    public MessageFormatterService(QueueProperties queueProperties) {
        this.queueProperties = queueProperties;
    }

    // ==================== Queue Messages ====================

    /**
     * Formats the "player joined queue" message.
     */
    public String formatPlayerJoinedQueue(long userId, Set<Long> queueSnapshot, int maxQueueSize) {
        String currentQueue = formatUserList(queueSnapshot);
        return String.format(
                "<@%s> has joined the queue! [%d/%d]\n Current queue: %s",
                userId,
                queueSnapshot.size(),
                maxQueueSize,
                queueSnapshot.isEmpty() ? "No players in queue" : currentQueue
        );
    }

    /**
     * Formats the "player left queue" message.
     */
    public String formatPlayerLeftQueue(long userId, Set<Long> queueSnapshot, int maxQueueSize) {
        String currentQueue = formatUserList(queueSnapshot);
        return String.format(
                "<@%s> has left the queue! [%d/%d]\n Current queue: %s",
                userId,
                queueSnapshot.size(),
                maxQueueSize,
                queueSnapshot.isEmpty() ? "No players in queue" : currentQueue
        );
    }

    /**
     * Formats the current queue status message.
     */
    public String formatQueueStatus(Set<Long> queueSnapshot, int maxQueueSize) {
        String currentQueue = formatUserList(queueSnapshot);
        return String.format(
                "Current queue [%d/%d]: %s",
                queueSnapshot.size(),
                maxQueueSize,
                queueSnapshot.isEmpty() ? "No players in queue" : currentQueue
        );
    }

    /**
     * Formats the "queue is full" message.
     */
    public String formatQueueFull() {
        return "Unable to join the queue. The queue is currently full!";
    }

    /**
     * Formats the "already in queue" message.
     */
    public String formatAlreadyInQueue() {
        return "You are already in the queue!";
    }

    /**
     * Formats the "not in queue" message.
     */
    public String formatNotInQueue() {
        return "Cannot leave queue. You are not currently in the queue!";
    }

    // ==================== Check-in Messages ====================

    /**
     * Formats the check-in started message.
     */
    public String formatCheckInStarted(Map<Long, Boolean> checkInStatus) {
        String playerStatusList = formatCheckInStatusList(checkInStatus);
        return String.format(
                "The queue has been filled!\n" +
                "Click the %s button within %s %s to accept.\n" +
                "Waiting for all players to accept...\n" +
                "%s",
                CHECKED_IN_SYMBOL,
                queueProperties.getCheckInTimeoutLength(),
                formatTimeoutUnit(),
                playerStatusList
        );
    }

    /**
     * Formats the check-in progress message (when a player checks in but not all have).
     */
    public String formatCheckInProgress(Map<Long, Boolean> checkInStatus) {
        String playerStatusList = formatCheckInStatusList(checkInStatus);
        return String.format(
                "The queue has been filled!\n" +
                "Click the %s button within %s %s to accept.\n" +
                "Waiting for all players to accept...\n" +
                "%s",
                CHECKED_IN_SYMBOL,
                queueProperties.getCheckInTimeoutLength(),
                formatTimeoutUnit(),
                playerStatusList
        );
    }

    /**
     * Formats the check-in completed message.
     */
    public String formatCheckInCompleted(Map<Long, Boolean> checkInStatus) {
        String playerStatusList = formatCheckInStatusList(checkInStatus);
        return String.format("All players have checked in.\n%s", playerStatusList);
    }

    /**
     * Formats the check-in cancelled message.
     */
    public String formatCheckInCancelled(long cancellingUserId, Set<Long> remainingQueue, int maxQueueSize) {
        if (remainingQueue.isEmpty()) {
            return String.format(
                    "Check-in cancelled by <@%d>\nNo players remaining in queue.",
                    cancellingUserId
            );
        }

        String queueList = formatUserList(remainingQueue);
        return String.format(
                "Check-in cancelled by <@%d>\nCurrent queue: %s [%d/%d]",
                cancellingUserId,
                queueList,
                remainingQueue.size(),
                maxQueueSize
        );
    }

    /**
     * Formats the "already checked in" message.
     */
    public String formatAlreadyCheckedIn() {
        return "You have already checked in!";
    }

    /**
     * Formats the "unauthorised check-in" message.
     */
    public String formatUnauthorisedCheckIn() {
        return "You are not a member of this check-in!";
    }

    /**
     * Formats the "no active session" message.
     */
    public String formatNoActiveSession() {
        return "There is no active check-in session.";
    }

    /**
     * Formats the "expired session" message.
     */
    public String formatExpiredSession() {
        return "This check-in session has expired.";
    }

    /**
     * Formats the check-in timeout message.
     * Displays which users did not check in and which users will be returned to the queue.
     *
     * @param usersWhoDidNotCheckIn Set of user IDs who failed to check in
     * @param usersWhoCheckedIn Set of user IDs who checked in (will be returned to queue)
     * @return Formatted timeout message
     */
    public String formatCheckInTimeout(Set<Long> usersWhoDidNotCheckIn, Set<Long> usersWhoCheckedIn) {
        StringBuilder message = new StringBuilder("Check-in has been cancelled.\n");

        if (usersWhoDidNotCheckIn.isEmpty()) {
            message.append("All players checked in on time.\n");
        } else {
            String notCheckedInList = formatUserList(usersWhoDidNotCheckIn);
            message.append("The following players did not check-in on time: ")
                   .append(notCheckedInList)
                   .append("\n");
        }

        if (usersWhoCheckedIn.isEmpty()) {
            message.append("No remaining players to return to the queue.");
        } else {
            String checkedInList = formatUserList(usersWhoCheckedIn);
            message.append("The following remaining players will be returned to the queue: ")
                   .append(checkedInList);
        }

        return message.toString();
    }

    // ==================== Helper Methods ====================

    /**
     * Formats a set of user IDs as Discord mentions.
     */
    public String formatUserList(Set<Long> userIds) {
        return userIds.stream()
                .map(id -> String.format("<@%d>", id))
                .collect(Collectors.joining(", "));
    }

    /**
     * Formats the check-in status list showing each player's status.
     */
    public String formatCheckInStatusList(Map<Long, Boolean> checkInStatus) {
        return checkInStatus.entrySet().stream()
                .map(entry -> String.format("<@%d> [%s]",
                        entry.getKey(),
                        entry.getValue() ? CHECKED_IN_SYMBOL : NOT_CHECKED_IN_SYMBOL))
                .collect(Collectors.joining(" | "));
    }

    private String formatTimeoutUnit() {
        String unit = queueProperties.getCheckInTimeoutUnit().toString().toLowerCase();
        if (queueProperties.getCheckInTimeoutLength() == 1) {
            return StringUtils.chop(unit); // Remove trailing 's' for singular
        }
        return unit;
    }

    // ==================== Spanner Messages ====================

    /**
     * Formats the spanner count message for a self-query.
     */
    public String formatSelfSpannerCount(int spannerCount) {
        String timesWord = spannerCount == 1 ? "time" : "times";
        return String.format("You have spannered **%d** %s", spannerCount, timesWord);
    }

    /**
     * Formats the spanner count message when querying another user.
     */
    public String formatUserSpannerCount(long targetUserId, int spannerCount) {
        String timesWord = spannerCount == 1 ? "time" : "times";
        return String.format("<@%d> has spannered **%d** %s", targetUserId, spannerCount, timesWord);
    }
}

