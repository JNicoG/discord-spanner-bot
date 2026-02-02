package io.github.jnicog.discord.spanner.bot.checkin;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

public class CheckInSession {

    private final long channelId;
    private final ConcurrentMap<Long, Boolean> userCheckInStatusMap;
    private long lastActiveCheckInMessageId;
    private ScheduledFuture<?> timeoutTask;

    public CheckInSession(long channelId, Set<Long> queueSnapshot) {
        this.channelId = channelId;
        this.userCheckInStatusMap = queueSnapshot.stream()
                .collect(Collectors.toConcurrentMap(userId -> userId, _ -> false));
    }

}
