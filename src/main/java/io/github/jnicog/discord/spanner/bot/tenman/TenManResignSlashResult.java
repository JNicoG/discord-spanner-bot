package io.github.jnicog.discord.spanner.bot.tenman;

import java.time.LocalDate;
import java.util.List;

public sealed interface TenManResignSlashResult
        permits TenManResignSlashResult.Resigned,
                TenManResignSlashResult.NoPoll,
                TenManResignSlashResult.NotOnRoster {

    record Resigned(
            long pollId,
            long channelId,
            long dateOptionId,
            LocalDate date,
            String timeDisplay,
            int slotsNeeded
    ) implements TenManResignSlashResult {}

    record NoPoll() implements TenManResignSlashResult {}

    record NotOnRoster() implements TenManResignSlashResult {}
}
