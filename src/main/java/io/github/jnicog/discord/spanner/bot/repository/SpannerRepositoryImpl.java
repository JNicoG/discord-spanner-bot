/*
package io.github.jnicog.discord.spanner.bot.repository;

import io.github.jnicog.discord.spanner.bot.model.Spanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class SpannerRepositoryImpl implements SpannerRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpannerRepositoryImpl.class);

    // In-memory storage until database is set up
    private final Map<Long, Spanner> spannerMap = new ConcurrentHashMap<>();

    public SpannerRepositoryImpl() {
        // Empty constructor
    }

    @Override
    public Spanner findByUserId(long userId) {
        return spannerMap.get(userId);
    }

    @Override
    public Integer getSpannerCount(long userId) {
        Spanner spanner = spannerMap.get(userId);
        return spanner != null ? spanner.getSpannerCount() : 0;
    }

    @Override
    public void incrementSpannerCount(long userId) {
        LOGGER.info("Incrementing spanner count for user {}", userId);

        Spanner spanner = spannerMap.computeIfAbsent(userId, Spanner::new);

        // Increment the count
        spanner.incrementSpannerCount();

        LOGGER.info("User {} now has {} spanners", userId, spanner.getSpannerCount());

        // When database is implemented, save the spanner here
        // spannerRepository.save(spanner);
    }

}
*/
