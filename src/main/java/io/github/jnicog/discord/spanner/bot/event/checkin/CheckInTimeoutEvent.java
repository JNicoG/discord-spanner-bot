package io.github.jnicog.discord.spanner.bot.event.checkin;

import io.github.jnicog.discord.spanner.bot.checkin.CheckInTimeoutResult;

import java.util.Set;

/**
 * Event published when a check-in session times out.
 * Users who did not check in are removed and NOT placed back into the queue.
 */
public class CheckInTimeoutEvent {

    private final CheckInTimeoutResult timeoutResult;

    public CheckInTimeoutEvent(CheckInTimeoutResult timeoutResult) {
        this.timeoutResult = timeoutResult;
    }

    public CheckInTimeoutResult getTimeoutResult() {
        return timeoutResult;
    }

    public long getChannelId() {
        return timeoutResult.channelId();
    }

    public long getMessageId() {
        return timeoutResult.messageId();
    }

    public Set<Long> getUsersWhoDidNotCheckIn() {
        return timeoutResult.usersWhoDidNotCheckIn();
    }

    public Set<Long> getUsersWhoCheckedIn() {
        return timeoutResult.usersWhoCheckedIn();
    }
}
