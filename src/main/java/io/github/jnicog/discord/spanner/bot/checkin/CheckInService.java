package io.github.jnicog.discord.spanner.bot.checkin;

import java.util.Map;
import java.util.Set;

public interface CheckInService {
    void startCheckIn(long channelId, Set<Long> queueSnapshot);
    void registerMessageId(long channelId, long messageId);
    CheckInAttemptResult userCheckIn(long channelId, long userId, long messageId);
    CheckInAttemptResult userCancel(long channelId, long userId);
    CheckInAttemptResult completeSession(long channelId);
    CheckInAttemptResult timeoutSession(long channelId);
    boolean hasActiveSession(long channelId);
    Map<Long, Boolean> getUpdatedCheckInSnapshot(long channelId);
}
