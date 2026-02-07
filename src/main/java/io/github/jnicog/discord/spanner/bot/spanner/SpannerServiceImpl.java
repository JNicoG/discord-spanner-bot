package io.github.jnicog.discord.spanner.bot.spanner;

import io.github.jnicog.discord.spanner.bot.repository.SpannerRepository;
import io.github.jnicog.discord.spanner.bot.repository.entity.SpannerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of SpannerService using JPA repository.
 */
@Service
@Transactional
public class SpannerServiceImpl implements SpannerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpannerServiceImpl.class);

    private final SpannerRepository spannerRepository;

    public SpannerServiceImpl(SpannerRepository spannerRepository) {
        this.spannerRepository = spannerRepository;
    }

    @Override
    public void incrementSpannerCount(long userId, long channelId) {
        SpannerEntity entity = spannerRepository.findByUserIdAndChannelId(userId, channelId)
                .orElseGet(() -> {
                    LOGGER.debug("Creating new spanner record for user {} in channel {}", userId, channelId);
                    return new SpannerEntity(userId, channelId);
                });

        entity.incrementSpannerCount();
        spannerRepository.save(entity);

        LOGGER.info("Incremented spanner count for user {} in channel {} to {}",
                userId, channelId, entity.getSpannerCount());
    }

    @Override
    @Transactional(readOnly = true)
    public int getSpannerCount(long userId, long channelId) {
        return spannerRepository.getSpannerCountByUserIdAndChannelId(userId, channelId)
                .orElse(0);
    }

    @Override
    @Transactional(readOnly = true)
    public int getTotalSpannerCount(long userId) {
        return spannerRepository.getTotalSpannerCountByUserId(userId);
    }
}

