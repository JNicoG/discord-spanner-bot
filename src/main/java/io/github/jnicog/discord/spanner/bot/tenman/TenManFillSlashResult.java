package io.github.jnicog.discord.spanner.bot.tenman;

import java.time.LocalDate;
import java.util.List;

public sealed interface TenManFillSlashResult
        permits TenManFillSlashResult.Filled,
                TenManFillSlashResult.NoPoll,
                TenManFillSlashResult.AlreadyOnRoster,
                TenManFillSlashResult.NoSlots {

    record Filled(
            boolean rosterRestored,
            long pollId,
            long channelId,
            long dateOptionId,
            LocalDate date,
            String timeDisplay,
            int newCount,
            int capacity,
            int slotsNeeded,
            List<Long> rosterUserIds
    ) implements TenManFillSlashResult {}

    record NoPoll() implements TenManFillSlashResult {}

    record AlreadyOnRoster() implements TenManFillSlashResult {}

    record NoSlots() implements TenManFillSlashResult {}
}
