package io.github.jnicog.discord.spanner.bot.checkin;

import io.github.jnicog.discord.spanner.bot.checkin.event.CheckInResult;

import java.util.Set;

public interface CheckInService {
    CheckInSession startCheckIn(long channelId, Set<Long> queueSnapshot);
    CheckInResult checkIn(long channelId, long userId);
    CheckInResult cancelCheckIn(long channelId, long userId);
    boolean hasActiveSession(long channelId);

}
