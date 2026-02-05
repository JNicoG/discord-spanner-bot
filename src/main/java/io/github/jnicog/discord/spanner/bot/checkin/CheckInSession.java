package io.github.jnicog.discord.spanner.bot.checkin;

import io.github.jnicog.discord.spanner.bot.event.CheckInResult;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class CheckInSession {

    private final long channelId;
    private final ConcurrentMap<Long, Boolean> userCheckInStatusMap;
    private final ReentrantLock lock = new ReentrantLock();

    public CheckInSession(long channelId, Set<Long> queueSnapshot) {
        this.channelId = channelId;
        this.userCheckInStatusMap = queueSnapshot.stream()
                .collect(Collectors.toConcurrentMap(userId -> userId, _ -> false));
    }

    public CheckInResult checkInUser(long userId) {
        lock.lock();
        try {
            if (!userCheckInStatusMap.containsKey(userId)) {
                return CheckInResult.UNAUTHORISED;
            }
            Boolean status = userCheckInStatusMap.get(userId);
            if (status == null) {
                throw new IllegalStateException("User check-in status is null for userId: " + userId);
            }
            if (!status) {
                userCheckInStatusMap.replace(userId, true);
                return CheckInResult.CHECKED_IN;
            }

            return CheckInResult.ALREADY_CHECKED_IN;

        } finally {
            lock.unlock();
        }
    }

    public CheckInResult cancelCheckIn(long userId) {
        lock.lock();
        try {
            if (!userCheckInStatusMap.containsKey(userId)) {
                return CheckInResult.UNAUTHORISED;
            }
            Boolean status = userCheckInStatusMap.get(userId);
            if (status == null) {
                throw new IllegalStateException("User check-in status is null for userId: " + userId);
            }
            if (status) {
                userCheckInStatusMap.replace(userId, false);
                return CheckInResult.CANCELLED;
            }

            return CheckInResult.NOT_CHECKED_IN;

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

}
