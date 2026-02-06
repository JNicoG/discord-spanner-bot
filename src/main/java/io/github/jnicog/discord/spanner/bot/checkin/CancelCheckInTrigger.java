package io.github.jnicog.discord.spanner.bot.checkin;

import io.github.jnicog.discord.spanner.bot.event.checkin.NoActiveSessionEvent;
import io.github.jnicog.discord.spanner.bot.event.checkin.PlayerCancelledCheckInEvent;
import io.github.jnicog.discord.spanner.bot.event.checkin.UnauthorisedCheckInEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CancelCheckInTrigger {

    private static final Logger LOGGER = LoggerFactory.getLogger(CancelCheckInTrigger.class);

    private final ApplicationEventPublisher eventPublisher;
    private final CheckInService checkInService;
//    private final SpannerRepository spannerRepository;

    public CancelCheckInTrigger(ApplicationEventPublisher eventPublisher, CheckInService checkInService) {
        this.eventPublisher = eventPublisher;
        this.checkInService = checkInService;
    }

    @EventListener
    public void onCancelCheckIn(PlayerCancelledCheckInEvent event) {
        long userId = event.getContext().userId();
        long channelId = event.getContext().channelId();

        CheckInResult checkInResult = checkInService.userCancel(channelId, userId);

        switch (checkInResult) {
            case UNAUTHORISED -> eventPublisher.publishEvent(new UnauthorisedCheckInEvent(event.getContext()));
            case SESSION_CANCELLED -> eventPublisher.publishEvent(
                    new PlayerCancelledCheckInEvent(event.getContext(), event.getActiveSession()));
            case NO_ACTIVE_SESSION -> eventPublisher.publishEvent(new NoActiveSessionEvent(event.getContext()));
            default -> throw new IllegalStateException("Unexpected check-in cancellation result: " + checkInResult);
        }

        // perform repository call
        // spannerRepository.incrementSpanner(userId, channelId);

    }
}
