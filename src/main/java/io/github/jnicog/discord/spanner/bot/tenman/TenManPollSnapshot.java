package io.github.jnicog.discord.spanner.bot.tenman;

import java.time.OffsetDateTime;
import java.util.List;

public record TenManPollSnapshot(
        long pollId,
        long channelId,
        long discordMessageId,
        String timeDisplay,
        boolean testMode,
        OffsetDateTime closesAt,
        List<TenManDateOptionSnapshot> dateOptions
) {}
