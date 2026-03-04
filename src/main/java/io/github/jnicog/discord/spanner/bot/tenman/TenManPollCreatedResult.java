package io.github.jnicog.discord.spanner.bot.tenman;

import java.time.OffsetDateTime;
import java.util.List;

public record TenManPollCreatedResult(
        long pollId,
        String timeDisplay,
        boolean testMode,
        OffsetDateTime closesAt,
        List<TenManDateOptionSnapshot> dateOptions
) {}
