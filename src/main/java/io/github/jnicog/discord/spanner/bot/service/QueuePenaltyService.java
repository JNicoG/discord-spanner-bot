package io.github.jnicog.discord.spanner.bot.service;

import io.github.jnicog.discord.spanner.bot.model.QueuePenalty;
import io.github.jnicog.discord.spanner.bot.model.SpannerId;

import java.time.Duration;
import java.util.Optional;

public interface QueuePenaltyService {

    /**
     * Check if a user is currently under cooldown for using /unkeen command
     * @param userId Discord user ID
     * @param channelId Discord channel ID
     * @return true if user is under cooldown, false otherwise
     */
    boolean isUserUnderCooldown(long userId, long channelId);

    /**
     * Get remaining cooldown duration for a user
     * @param userId Discord user ID
     * @param channelId Discord channel ID
     * @return Duration of remaining cooldown, or Duration.ZERO if no cooldown
     */
    Duration getRemainingCooldown(long userId, long channelId);

    /**
     * Apply penalty when user uses /unkeen command
     * @param userId Discord user ID
     * @param channelId Discord channel ID
     * @return The penalty tier that was applied (1, 2, or 3)
     */
    int applyPenalty(long userId, long channelId);

    /**
     * Get current penalty information for a user
     * @param userId Discord user ID
     * @param channelId Discord channel ID
     * @return Optional containing penalty info, or empty if no penalty exists
     */
    Optional<QueuePenalty> getPenaltyInfo(long userId, long channelId);

    /**
     * Process penalty decay for all eligible users (called by scheduled task)
     */
    void processAllPenaltyDecay();

    /**
     * Cleanup expired penalties with tier 0
     */
    void cleanupExpiredPenalties();

}