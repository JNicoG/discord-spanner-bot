package io.github.jnicog.discord.spanner.bot.checkin;

import io.github.jnicog.discord.spanner.bot.event.CheckInResult;

import java.util.Set;

public interface CheckInService {
    void startCheckIn(long channelId, Set<Long> queueSnapshot);
    CheckInResult userCheckIn(long channelId, long userId);
    CheckInResult userCancel(long channelId, long userId);
    CheckInResult completeSession(long channelId);
    CheckInResult timeoutSession(long channelId);
    boolean hasActiveSession(long channelId);
}
