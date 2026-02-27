package io.github.jnicog.discord.spanner.bot.tenman;

import java.time.LocalDate;
import java.util.List;

public record TenManDateOptionSnapshot(
        long id,
        LocalDate date,
        List<Long> signedUpUserIds
) {}
