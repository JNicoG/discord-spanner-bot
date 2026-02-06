package io.github.jnicog.discord.spanner.bot.checkin;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class CheckInSession {

    private long messageId;
    private final long channelId;
    private final ConcurrentMap<Long, Boolean> userCheckInStatusMap;
    private final ReentrantLock lock = new ReentrantLock();

    public CheckInSession(long channelId, Set<Long> queueSnapshot) {
        this.channelId = channelId;
        this.userCheckInStatusMap = queueSnapshot.stream()
                .collect(Collectors.toConcurrentMap(userId -> userId, _ -> false));
    }

    /**
     * Allows a user to check-in to the active session.
     * A user can check-in only once, and must be part of the session (i.e. in the queue snapshot) to check-in.
     * @param userId ID of the user attempting to check-in.
     * @return CheckInResult indicating the outcome of the check-in attempt.
     */
    public CheckInAttemptResult checkInUser(long userId) {
        lock.lock();
        try {
            if (!userCheckInStatusMap.containsKey(userId)) {
                return CheckInAttemptResult.UNAUTHORISED;
            }

            Boolean status = userCheckInStatusMap.get(userId);
            if (status == null) {
                throw new IllegalStateException("User check-in status is null for userId: " + userId);
            }

            if (!status) {
                userCheckInStatusMap.replace(userId, true);
                return CheckInAttemptResult.CHECKED_IN;
            }

            return CheckInAttemptResult.ALREADY_CHECKED_IN;

        } finally {
            lock.unlock();
        }
    }

    /**
     * Allows a user to cancel the check-in session i.e. pressing the "Spanner" button.
     * This will cancel the check-in session for everyone, and issues a spanner to the offending user.
     * @param userId ID of the user requesting to cancel the check-in session.
     * @return CheckInResult indicating the outcome of the cancellation attempt.
     */
    public CheckInAttemptResult cancelCheckIn(long userId) {
        lock.lock();
        try {
            // Authorisation check: only users who are part of the session can cancel it
            if (!userCheckInStatusMap.containsKey(userId)) {
                return CheckInAttemptResult.UNAUTHORISED;
            }

            Boolean status = userCheckInStatusMap.get(userId);
            if (status == null) {
                throw new IllegalStateException("User check-in status is null for userId: " + userId);
            }

            return CheckInAttemptResult.SESSION_CANCELLED;

        } finally {
            lock.unlock();
        }
    }

    public Set<Long> getCheckedInUsers() {
        lock.lock();
        try {
            return userCheckInStatusMap.entrySet().stream()
                    .filter(Map.Entry::getValue)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());
        } finally {
            lock.unlock();
        }
    }

    public Set<Long> getNotCheckedInUsers() {
        lock.lock();
        try {
            return userCheckInStatusMap.entrySet().stream()
                    .filter(entry -> !entry.getValue())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());
        } finally {
            lock.unlock();
        }
    }

    public long getChannelId() {
        return channelId;
    }

    public Map<Long, Boolean> getUserCheckInStatusSnapshot() {
        lock.lock();
        try {
            return Collections.unmodifiableMap(userCheckInStatusMap);
        } finally {
            lock.unlock();
        }
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

}
