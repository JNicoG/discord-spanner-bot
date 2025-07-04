package io.github.jnicog.discord.spanner.bot.service;

public interface SpannerService {

    int getSpannerCount(Long userId);

    void incrementSpannerCount(Long userId);

}
