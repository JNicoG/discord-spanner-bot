package io.github.jnicog.discord.spanner.bot.service;

public interface SpannerService {

    int getSpannerCount(Long userId, Long channelId);

    void incrementSpannerCount(Long userId, Long channelId);

}
