package io.github.jnicog.discord.spanner.bot.service;

import io.github.jnicog.discord.spanner.bot.model.Spanner;
import io.github.jnicog.discord.spanner.bot.repository.SpannerRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class SpannerServiceImpl implements SpannerService {

    private final SpannerRepository spannerRepository;

    public SpannerServiceImpl(SpannerRepository spannerRepository) {
        this.spannerRepository = spannerRepository;
    }

    @Override
    @Transactional
    public void incrementSpannerCount(Long userId) {
        Spanner spanner = spannerRepository.findByUserId(userId);

        if (spanner == null) {
            spanner = new Spanner(userId);
            spanner.incrementSpannerCount();
            spannerRepository.save(spanner);
        } else {
            spannerRepository.incrementSpannerCount(userId);
        }
    }

    @Override
    public int getSpannerCount(Long userId) {
        Integer count = spannerRepository.getSpannerCount(userId);
        return count != null ? count : 0;
    }

}
