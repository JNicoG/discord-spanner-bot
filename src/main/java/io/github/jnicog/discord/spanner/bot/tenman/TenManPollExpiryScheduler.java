package io.github.jnicog.discord.spanner.bot.tenman;

import io.github.jnicog.discord.spanner.bot.event.tenman.TenManPollExpiredEvent;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManPollExpiryWarningEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TenManPollExpiryScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TenManPollExpiryScheduler.class);
    private static final Duration WARNING_WINDOW = Duration.ofHours(1);

    private final TenManService tenManService;
    private final ApplicationEventPublisher eventPublisher;
    private final Set<Long> warnedPollIds = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public TenManPollExpiryScheduler(TenManService tenManService, ApplicationEventPublisher eventPublisher) {
        this.tenManService = tenManService;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(fixedDelay = 60_000)
    public void checkExpiredPolls() {
        tenManService.processExpiredPolls().forEach(snapshot -> {
            LOGGER.info("Ten-man poll {} in channel {} expired — publishing expiry event", snapshot.pollId(), snapshot.channelId());
            warnedPollIds.remove(snapshot.pollId());
            eventPublisher.publishEvent(new TenManPollExpiredEvent(snapshot));
        });
    }

    @Scheduled(fixedDelay = 60_000)
    public void checkClosingSoonPolls() {
        tenManService.getPollsClosingSoon(WARNING_WINDOW).forEach(snapshot -> {
            if (warnedPollIds.add(snapshot.pollId())) {
                LOGGER.info("Ten-man poll {} in channel {} closing soon — publishing warning event", snapshot.pollId(), snapshot.channelId());
                eventPublisher.publishEvent(new TenManPollExpiryWarningEvent(snapshot));
            }
        });
    }
}
