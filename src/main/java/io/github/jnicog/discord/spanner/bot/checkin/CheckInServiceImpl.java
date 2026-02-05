package io.github.jnicog.discord.spanner.bot.checkin;

import io.github.jnicog.discord.spanner.bot.event.CheckInResult;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class CheckInServiceImpl implements CheckInService {

    // Change to use QueueProperties if needed
    private static final Duration CHECK_IN_TIMEOUT = Duration.of(5, ChronoUnit.MINUTES);

    private final ConcurrentMap<Long, CheckInSession> activeSessions = new ConcurrentHashMap<>();

    public CheckInServiceImpl() {
        // Empty constructor
    }

    @Override
    public void startCheckIn(long channelId, Set<Long> queueSnapshot) {
        CheckInSession session = new CheckInSession(channelId, queueSnapshot);
        activeSessions.put(channelId, session);

//        ScheduledFuture<?> future = taskScheduler.schedule(() -> {
//            handleTimeout(channelId),
//            Instant.now().plus(CHECK_IN_TIMEOUT);
//        });
//        session.setTimeoutFuture(future);
    }

    @Override
    public CheckInResult userCheckIn(long channelId, long userId) {
        CheckInSession session = activeSessions.get(channelId);
        if (session == null) {
            return CheckInResult.NO_ACTIVE_SESSION;
        }


        return null;
    }

    @Override
    public CheckInResult userCancel(long channelId, long userId) {
        return null;
    }

    @Override
    public CheckInResult completeSession(long channelId) {
        return null;
    }

    @Override
    public CheckInResult timeoutSession(long channelId) {
        return null;
    }

    @Override
    public boolean hasActiveSession(long channelId) {
        return false;
    }
}
