package io.github.jnicog.discord.spanner.bot.leaderboard;

import java.time.OffsetDateTime;
import java.util.concurrent.ScheduledFuture;

/**
 * Represents an active leaderboard session for a user.
 * Tracks the message ID, channel, owner, and current page for pagination.
 */
public class LeaderboardSession {

    private final long messageId;
    private final long channelId;
    private final long ownerId;
    private final OffsetDateTime createdAt;
    private int currentPage;
    private ScheduledFuture<?> timeoutFuture;

    public LeaderboardSession(long messageId, long channelId, long ownerId) {
        this.messageId = messageId;
        this.channelId = channelId;
        this.ownerId = ownerId;
        this.currentPage = 1;
        this.createdAt = OffsetDateTime.now();
    }

    public long getMessageId() {
        return messageId;
    }

    public long getChannelId() {
        return channelId;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public ScheduledFuture<?> getTimeoutFuture() {
        return timeoutFuture;
    }

    public void setTimeoutFuture(ScheduledFuture<?> timeoutFuture) {
        this.timeoutFuture = timeoutFuture;
    }

    /**
     * Checks if the given user is the owner of this session.
     */
    public boolean isOwner(long userId) {
        return this.ownerId == userId;
    }

    /**
     * Cancels the timeout future if it exists.
     */
    public void cancelTimeout() {
        if (timeoutFuture != null && !timeoutFuture.isDone()) {
            timeoutFuture.cancel(false);
        }
    }
}

