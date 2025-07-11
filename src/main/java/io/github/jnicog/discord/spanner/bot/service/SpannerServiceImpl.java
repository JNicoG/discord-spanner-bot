package io.github.jnicog.discord.spanner.bot.service;

import io.github.jnicog.discord.spanner.bot.model.Spanner;
import io.github.jnicog.discord.spanner.bot.repository.SpannerRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SpannerServiceImpl implements SpannerService {

    private final SpannerRepository spannerRepository;

    public SpannerServiceImpl(SpannerRepository spannerRepository) {
        this.spannerRepository = spannerRepository;
    }

    @Override
    public int getSpannerCount(Long userId) {
        int id = userId.intValue();
        return spannerRepository.findById(id).map(Spanner::getSpannerCount).orElse(0);
    }

    @Override
    public void incrementSpannerCount(Long userId) {
        int id = userId.intValue();
        Optional<Spanner> spanner = spannerRepository.findById(id);
        if (spanner.isPresent()) {
            spanner.get().incrementSpannerCount();
            spannerRepository.save(spanner.get());
        } else {
            Spanner newSpanner = new Spanner(userId);
            newSpanner.incrementSpannerCount();
            spannerRepository.save(newSpanner);
        }
    }

}
