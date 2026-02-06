package io.github.jnicog.discord.spanner.bot.checkin;

import io.github.jnicog.discord.spanner.bot.event.checkin.CheckInStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class CheckInServiceImpl implements CheckInService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckInServiceImpl.class);

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
            throw new IllegalStateException("Failed to register check-in message ID: No active check-in session found for channelId: " + channelId);
        }
        session.setMessageId(messageId);
    }

    @Override
    public CheckInAttemptResult userCheckIn(long channelId, long userId, long messageId) {
        CheckInSession session = activeSessions.get(channelId);
        if (session == null) {
            return CheckInAttemptResult.NO_ACTIVE_SESSION;
        }
        LOGGER.debug("User {} attempting to check-in to channel {} with messageId {}. Current session messageId: {}", userId, channelId, messageId, session.getMessageId());
        if (messageId != session.getMessageId()) {
            return CheckInAttemptResult.EXPIRED_SESSION;
        }

        CheckInAttemptResult result = session.checkInUser(userId);

        // If all users have checked in, atomically remove the session only if it's still the same instance
        if (result == CheckInAttemptResult.SESSION_COMPLETED) {
            LOGGER.info("All users have checked in for channel {}. Session completed.", channelId);
            activeSessions.remove(channelId, session);  // Atomic: only removes if session is still the same
        }

        return result;
    }

    @Override
    public CheckInAttemptResult userCancel(long channelId, long userId) {
        CheckInSession session = activeSessions.get(channelId);
        if (session == null) {
            return CheckInAttemptResult.NO_ACTIVE_SESSION;
        }

        CheckInAttemptResult result = session.cancelCheckIn(userId);

        if (result == CheckInAttemptResult.SESSION_CANCELLED) {
            // Atomic: only removes if session is still the same instance
            activeSessions.remove(channelId, session);
        }

        return result;
    }

    @Override
    public CheckInAttemptResult completeSession(long channelId) {
        return null;
    }

    @Override
    public CheckInAttemptResult timeoutSession(long channelId) {
        return null;
    }

    @Override
    public boolean hasActiveSession(long channelId) {
        return activeSessions.containsKey(channelId);
    }

    @Override
    public Map<Long, Boolean> getUpdatedCheckInSnapshot(long channelId) {
        CheckInSession session = activeSessions.get(channelId);
        if (session == null) {
            throw new IllegalStateException("Failed to get check-in snapshot: No active check-in session found for channelId: " + channelId);
        }
        return session.getUserCheckInStatusSnapshot();
    }

    @Override
    public Set<Long> getSessionParticipants(long channelId) {
        CheckInSession session = activeSessions.get(channelId);
        if (session == null) {
            return Set.of();
        }
        return session.getUserCheckInStatusSnapshot().keySet();
    }

    @Override
    public CancelResult cancelAndGetRemainingUsers(long channelId, long cancellingUserId) {
        CheckInSession session = activeSessions.get(channelId);
        if (session == null) {
            return CancelResult.noActiveSession();
        }

        // Get the message ID before cancelling (volatile read is safe)
        long messageId = session.getMessageId();

        // Get all participants except the cancelling user (snapshot is a copy)
        Set<Long> allParticipants = session.getUserCheckInStatusSnapshot().keySet();
        Set<Long> remainingUsers = allParticipants.stream()
                .filter(userId -> userId != cancellingUserId)
                .collect(java.util.stream.Collectors.toSet());

        CheckInAttemptResult result = session.cancelCheckIn(cancellingUserId);

        if (result == CheckInAttemptResult.SESSION_CANCELLED) {
            // Atomic: only removes if session is still the same instance
            activeSessions.remove(channelId, session);
            return CancelResult.cancelled(remainingUsers, messageId);
        } else if (result == CheckInAttemptResult.UNAUTHORISED) {
            return CancelResult.unauthorised();
        }

        return new CancelResult(result, Set.of(), messageId);
    }
}
