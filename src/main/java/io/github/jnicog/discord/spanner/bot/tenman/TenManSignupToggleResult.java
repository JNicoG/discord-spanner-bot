package io.github.jnicog.discord.spanner.bot.tenman;

public record TenManSignupToggleResult(
        boolean added,
        long pollId,
        long dateOptionId,
        int newCount,
        boolean dateFull
) {}
