package io.github.jnicog.discord.spanner.bot.repository;

import io.github.jnicog.discord.spanner.bot.model.Spanner;
import org.springframework.stereotype.Service;

@Service
public class SpannerRepositoryImpl implements SpannerRepository {

    public SpannerRepositoryImpl() {
        // Empty constructor
    }

    @Override
    public Spanner findByUserId(long userId) {
        return null;
    }

    @Override
    public int getSpannerCount(long userId) {
        return 0;
    }

    @Override
    public void incrementSpannerCount(long userId) {

    }
}
