package io.github.jnicog.discord.spanner.bot.tenman;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class TenManCooldownService {

    static final Duration COOLDOWN = Duration.ofSeconds(10);

    private final Cache<String, Instant> lastAction = CacheBuilder.newBuilder()
            .expireAfterWrite(COOLDOWN)
            .build();

    public boolean isOnCooldown(long userId, long dateOptionId) {
        Instant last = lastAction.getIfPresent(key(userId, dateOptionId));
        return last != null && Instant.now().isBefore(last.plus(COOLDOWN));
    }

    public Duration remainingCooldown(long userId, long dateOptionId) {
        Instant last = lastAction.getIfPresent(key(userId, dateOptionId));
        if (last == null) return Duration.ZERO;
        Duration remaining = Duration.between(Instant.now(), last.plus(COOLDOWN));
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }

    public void recordAction(long userId, long dateOptionId) {
        lastAction.put(key(userId, dateOptionId), Instant.now());
    }

    private String key(long userId, long dateOptionId) {
        return userId + ":" + dateOptionId;
    }
}
