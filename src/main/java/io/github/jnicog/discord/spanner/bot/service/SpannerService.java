package io.github.jnicog.discord.spanner.bot.service;

import net.dv8tion.jda.api.entities.User;

public interface SpannerService {

    void incrementSpannerCount(Long userId);

}
