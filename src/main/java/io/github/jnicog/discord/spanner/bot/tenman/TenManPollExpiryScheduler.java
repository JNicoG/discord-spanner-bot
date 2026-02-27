package io.github.jnicog.discord.spanner.bot.tenman;

import io.github.jnicog.discord.spanner.bot.event.tenman.TenManPollExpiredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TenManPollExpiryScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TenManPollExpiryScheduler.class);

    private final TenManService tenManService;
    private final ApplicationEventPublisher eventPublisher;

    public TenManPollExpiryScheduler(TenManService tenManService, ApplicationEventPublisher eventPublisher) {
        this.tenManService = tenManService;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(fixedDelay = 60_000)
    public void checkExpiredPolls() {
        tenManService.processExpiredPolls().forEach(snapshot -> {
            LOGGER.info("Ten-man poll {} in channel {} expired — publishing expiry event", snapshot.pollId(), snapshot.channelId());
            eventPublisher.publishEvent(new TenManPollExpiredEvent(snapshot));
        });
    }
}
