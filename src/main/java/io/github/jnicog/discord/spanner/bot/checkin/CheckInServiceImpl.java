package io.github.jnicog.discord.spanner.bot.checkin;

import io.github.jnicog.discord.spanner.bot.checkin.event.CheckInResult;
import io.github.jnicog.discord.spanner.bot.config.QueueProperties;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;

@Service
public class CheckInServiceImpl implements CheckInService {

    // Change to use QueueProperties if needed
    private static final Duration CHECK_IN_TIMEOUT = Duration.of(5, ChronoUnit.MINUTES);

    private final ConcurrentMap<Long, CheckInSession> activeSessions = new ConcurrentHashMap<>();

    public CheckInServiceImpl() {
        // Empty constructor
    }

    @Override
    public CheckInSession startCheckIn(long channelId, Set<Long> queueSnapshot) {
        CheckInSession session = new CheckInSession(channelId, queueSnapshot);
        activeSessions.put(channelId, session);

//        ScheduledFuture<?> future = taskScheduler.schedule(() -> {
//            handleTimeout(channelId),
//            Instant.now().plus(CHECK_IN_TIMEOUT);
//        });
//        session.setTimeoutFuture(future);
        return null;
    }

    @Override
    public CheckInResult checkIn(long channelId, long userId) {
        CheckInSession session = activeSessions.get(channelId);
        if (session == null) {
            return CheckInResult.NO_ACTIVE_SESSION;
        }


        return null;
    }

    @Override
    public CheckInResult cancelCheckIn(long channelId, long userId) {
        return null;
    }

    @Override
    public boolean hasActiveSession(long channelId) {
        return false;
    }
}
