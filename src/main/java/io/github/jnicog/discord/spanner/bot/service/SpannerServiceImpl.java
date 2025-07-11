package io.github.jnicog.discord.spanner.bot.service;

import io.github.jnicog.discord.spanner.bot.model.Spanner;
import io.github.jnicog.discord.spanner.bot.model.SpannerId;
import io.github.jnicog.discord.spanner.bot.repository.SpannerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SpannerServiceImpl implements SpannerService {

    private final SpannerRepository spannerRepository;

    public SpannerServiceImpl(SpannerRepository spannerRepository) {
        this.spannerRepository = spannerRepository;
    }

    @Override
    public int getSpannerCount(Long userId, Long channelId) {
        SpannerId spannerId = new SpannerId(userId, channelId);
        return spannerRepository.findById(spannerId).map(Spanner::getSpannerCount).orElse(0);
    }

    @Override
    public void incrementSpannerCount(Long userId, Long channelId) {
        SpannerId spannerId = new SpannerId(userId, channelId);
        Optional<Spanner> spanner = spannerRepository.findById(spannerId);
        if (spanner.isPresent()) {
            spanner.get().incrementSpannerCount();
            spannerRepository.save(spanner.get());
        } else {
            Spanner newSpanner = new Spanner(userId, channelId);
            newSpanner.incrementSpannerCount();
            spannerRepository.save(newSpanner);
        }
    }

    @Override
    public Page<Spanner> getLeaderboard(Long channelId, int page, int size) {
        return spannerRepository.findByChannelIdOrderBySpannerCountDesc(channelId,
                PageRequest.of(page, size));
    }

}
