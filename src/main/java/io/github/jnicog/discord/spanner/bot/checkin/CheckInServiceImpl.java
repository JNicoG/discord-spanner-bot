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
    }

    @Override
    public void registerMessageId(long channelId, long messageId) {
        activeSessions.compute(channelId, (key, session) -> {
            if (session == null) {
                throw new IllegalStateException("Failed to register check-in message ID: No active check-in session found for channelId: " + channelId);
            }
            session.setMessageId(messageId);
            return session;
        });
    }

    @Override
    public CheckInAttemptResult userCheckIn(long channelId, long userId, long messageId) {
        // Use a holder to capture the result from within compute
        final CheckInAttemptResult[] resultHolder = new CheckInAttemptResult[1];

        activeSessions.compute(channelId, (key, session) -> {
            if (session == null) {
                resultHolder[0] = CheckInAttemptResult.NO_ACTIVE_SESSION;
                return null;
            }

            LOGGER.debug("User {} attempting to check-in to channel {} with messageId {}. Current session messageId: {}",
                    userId, channelId, messageId, session.getMessageId());

            if (messageId != session.getMessageId()) {
                resultHolder[0] = CheckInAttemptResult.EXPIRED_SESSION;
                return session;
            }

            CheckInAttemptResult result = session.checkInUser(userId);
            resultHolder[0] = result;

            // If all users have checked in, remove the session
            if (result == CheckInAttemptResult.SESSION_COMPLETED) {
                LOGGER.info("All users have checked in for channel {}. Session completed.", channelId);
                return null; // Remove the session
            }

            return session;
        });

        return resultHolder[0];
    }

    @Override
    public CheckInAttemptResult userCancel(long channelId, long userId) {
        // Use a holder to capture the result from within compute
        final CheckInAttemptResult[] resultHolder = new CheckInAttemptResult[1];

        activeSessions.compute(channelId, (key, session) -> {
            if (session == null) {
                resultHolder[0] = CheckInAttemptResult.NO_ACTIVE_SESSION;
                return null;
            }

            CheckInAttemptResult result = session.cancelCheckIn(userId);
            resultHolder[0] = result;

            if (result == CheckInAttemptResult.SESSION_CANCELLED) {
                return null; // Remove the session
            }

            return session;
        });

        return resultHolder[0];
    }

    @Override
    public CheckInAttemptResult completeSession(long channelId) {
        return null;
    }

    @Override
    public CheckInTimeoutResult timeoutSession(long channelId) {
        // Use a holder to capture the result from within compute
        final CheckInTimeoutResult[] resultHolder = new CheckInTimeoutResult[1];

        activeSessions.compute(channelId, (key, session) -> {
            if (session == null) {
                resultHolder[0] = CheckInTimeoutResult.noActiveSession(channelId);
                return null;
            }

            long messageId = session.getMessageId();
            Set<Long> checkedIn = session.getCheckedInUsers();
            Set<Long> notCheckedIn = session.getNotCheckedInUsers();

            resultHolder[0] = new CheckInTimeoutResult(
                    CheckInAttemptResult.SESSION_TIMED_OUT,
                    notCheckedIn,
                    checkedIn,
                    messageId,
                    channelId
            );

            return null; // Remove the session
        });

        return resultHolder[0];
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
        // Use a holder to capture the result from within compute
        final CancelResult[] resultHolder = new CancelResult[1];

        activeSessions.compute(channelId, (key, session) -> {
            if (session == null) {
                resultHolder[0] = CancelResult.noActiveSession();
                return null;
            }

            // Get the message ID before cancelling (within lock context of compute)
            long messageId = session.getMessageId();

            // Get all participants except the cancelling user (snapshot is a copy)
            Set<Long> allParticipants = session.getUserCheckInStatusSnapshot().keySet();
            Set<Long> remainingUsers = allParticipants.stream()
                    .filter(userId -> userId != cancellingUserId)
                    .collect(java.util.stream.Collectors.toSet());

            CheckInAttemptResult result = session.cancelCheckIn(cancellingUserId);

            if (result == CheckInAttemptResult.SESSION_CANCELLED) {
                resultHolder[0] = CancelResult.cancelled(remainingUsers, messageId);
                return null; // Remove the session
            } else if (result == CheckInAttemptResult.UNAUTHORISED) {
                resultHolder[0] = CancelResult.unauthorised();
                return session; // Keep the session
            }

            resultHolder[0] = new CancelResult(result, Set.of(), messageId);
            return session;
        });

        return resultHolder[0];
    }
}
