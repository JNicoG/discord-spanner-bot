package io.github.jnicog.discord.spanner.bot.checkin;

import io.github.jnicog.discord.spanner.bot.event.checkin.CheckInStartedEvent;
import org.springframework.context.ApplicationEventPublisher;
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

    private final ApplicationEventPublisher eventPublisher;
    private final ConcurrentMap<Long, CheckInSession> activeSessions = new ConcurrentHashMap<>();

    public CheckInServiceImpl(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void startCheckIn(long channelId, Set<Long> queueSnapshot) {
        CheckInSession session = new CheckInSession(channelId, queueSnapshot);
        activeSessions.put(channelId, session);

        CheckInStartedEvent checkInStartedEvent = new CheckInStartedEvent(session);
        eventPublisher.publishEvent(checkInStartedEvent);

        // Schedule timeout task to cancel session after CHECK_IN_TIMEOUT duration
//        ScheduledFuture<?> future = taskScheduler.schedule(() -> {
//            handleTimeout(channelId),
//            Instant.now().plus(CHECK_IN_TIMEOUT);
//        });
//        session.setTimeoutFuture(future);
    }

    @Override
    public void registerMessageId(long channelId, long messageId) {
        CheckInSession session = activeSessions.get(channelId);
        if (session == null) {
            throw new IllegalStateException("No active check-in session found for channelId: " + channelId);
        }
        session.setMessageId(messageId);
    }

    @Override
    public CheckInResult userCheckIn(long channelId, long userId) {
        CheckInSession session = activeSessions.get(channelId);
        if (session == null) {
            return CheckInResult.NO_ACTIVE_SESSION;
        }

        return session.checkInUser(userId);
    }

    @Override
    public CheckInResult userCancel(long channelId, long userId) {
        CheckInSession session = activeSessions.get(channelId);
        if (session == null) {
            return CheckInResult.NO_ACTIVE_SESSION;
        }

        CheckInResult result = session.cancelCheckIn(userId);

        if (result == CheckInResult.SESSION_CANCELLED) {
            // atomic removal
            activeSessions.remove(channelId);

            // perform repository call
            // spannerRepository.incrementSpanner(userId, channelId);

            // cancel the timeout task
            // session.getTimeoutFuture().cancel();
        }

        return result;
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
        return activeSessions.containsKey(channelId);
    }
}
