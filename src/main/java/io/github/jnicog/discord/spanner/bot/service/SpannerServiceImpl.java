/*
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

    @Transactional
    public void addSpanner(int userId) {
        Spanner spanner = spannerRepository.findByUserId(userId);
        if (spanner == null) {
            spanner = new Spanner(userId);
        }
        spanner.incrementSpannerCount();

        spannerRepository.save(spanner);
    }

}
*/
