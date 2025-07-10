package io.github.jnicog.discord.spanner.bot.service;

import io.github.jnicog.discord.spanner.bot.model.Spanner;
import org.springframework.data.domain.Page;

public interface SpannerService {

    int getSpannerCount(Long userId, Long channelId);

    void incrementSpannerCount(Long userId, Long channelId);

    Page<Spanner> getLeaderboard(Long channelId, int page, int size);
}
