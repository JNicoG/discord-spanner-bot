package io.github.jnicog.discord.spanner.bot.service;

import io.github.jnicog.discord.spanner.bot.model.QueuePenalty;
import io.github.jnicog.discord.spanner.bot.model.SpannerId;
import io.github.jnicog.discord.spanner.bot.repository.QueuePenaltyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class QueuePenaltyServiceImpl implements QueuePenaltyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueuePenaltyServiceImpl.class);

    // Penalty tier durations
    private static final Duration TIER_1_DURATION = Duration.ofMinutes(1);
    private static final Duration TIER_2_DURATION = Duration.ofHours(1);
    private static final Duration TIER_3_DURATION = Duration.ofDays(1);

    private final QueuePenaltyRepository queuePenaltyRepository;

    @Autowired
    public QueuePenaltyServiceImpl(QueuePenaltyRepository queuePenaltyRepository) {
        this.queuePenaltyRepository = queuePenaltyRepository;
    }

    @Override
    public boolean isUserUnderCooldown(long userId, long channelId) {
        SpannerId spannerId = new SpannerId(userId, channelId);
        Optional<QueuePenalty> penaltyOpt = queuePenaltyRepository.findById(spannerId);
        
        return penaltyOpt.map(QueuePenalty::isUnderCooldown).orElse(false);
    }

    @Override
    public Duration getRemainingCooldown(long userId, long channelId) {
        SpannerId spannerId = new SpannerId(userId, channelId);
        Optional<QueuePenalty> penaltyOpt = queuePenaltyRepository.findById(spannerId);
        
        if (penaltyOpt.isEmpty()) {
            return Duration.ZERO;
        }
        
        QueuePenalty penalty = penaltyOpt.get();
        if (!penalty.isUnderCooldown()) {
            return Duration.ZERO;
        }
        
        return Duration.ofMillis(penalty.getRemainingCooldownMillis());
    }

    @Override
    @Transactional
    public int applyPenalty(long userId, long channelId) {
        SpannerId spannerId = new SpannerId(userId, channelId);
        Optional<QueuePenalty> existingPenaltyOpt = queuePenaltyRepository.findById(spannerId);
        
        QueuePenalty penalty;
        int newTier;
        
        if (existingPenaltyOpt.isEmpty()) {
            // First penalty for this user
            penalty = new QueuePenalty(userId, channelId);
            newTier = 1;
        } else {
            penalty = existingPenaltyOpt.get();
            
            // Check if last penalty was within the last 24 hours
            Instant oneDayAgo = Instant.now().minus(1, ChronoUnit.DAYS);
            boolean recentPenalty = penalty.getLastPenaltyTime().isAfter(oneDayAgo);
            
            if (recentPenalty) {
                // Escalate tier (max tier 3)
                newTier = Math.min(penalty.getCurrentTier() + 1, 3);
            } else {
                // Reset to tier 1 if last penalty was more than 24 hours ago
                newTier = 1;
            }
        }
        
        // Apply new penalty
        penalty.setCurrentTier(newTier);
        penalty.setLastPenaltyTime(Instant.now());
        penalty.setCooldownExpiresAt(calculateCooldownExpiry(newTier));
        
        queuePenaltyRepository.save(penalty);
        
        LOGGER.info("Applied tier {} penalty to user {} in channel {} (cooldown until {})", 
                   newTier, userId, channelId, penalty.getCooldownExpiresAt());
        
        return newTier;
    }

    @Override
    public Optional<QueuePenalty> getPenaltyInfo(long userId, long channelId) {
        SpannerId spannerId = new SpannerId(userId, channelId);
        return queuePenaltyRepository.findById(spannerId);
    }

    @Override
    @Transactional
    public void processAllPenaltyDecay() {
        Instant now = Instant.now();
        Instant dayAgo = now.minus(1, ChronoUnit.DAYS);
        
        List<QueuePenalty> penaltiesToDecay = queuePenaltyRepository.findPenaltiesEligibleForDecay(now, dayAgo);
        
        for (QueuePenalty penalty : penaltiesToDecay) {
            int newTier = penalty.getCurrentTier() - 1;
            penalty.setCurrentTier(newTier);
            
            LOGGER.info("Decayed penalty tier for user {} in channel {} from {} to {}", 
                       penalty.getUserId(), penalty.getChannelId(), penalty.getCurrentTier() + 1, newTier);
        }
        
        if (!penaltiesToDecay.isEmpty()) {
            queuePenaltyRepository.saveAll(penaltiesToDecay);
            LOGGER.info("Processed penalty decay for {} users", penaltiesToDecay.size());
        }
    }

    @Override
    @Transactional
    public void cleanupExpiredPenalties() {
        Instant now = Instant.now();
        List<QueuePenalty> penaltiesToCleanup = queuePenaltyRepository.findPenaltiesToCleanup(now);
        
        if (!penaltiesToCleanup.isEmpty()) {
            queuePenaltyRepository.deleteAll(penaltiesToCleanup);
            LOGGER.info("Cleaned up {} expired penalty records", penaltiesToCleanup.size());
        }
    }

    private Instant calculateCooldownExpiry(int tier) {
        Instant now = Instant.now();
        return switch (tier) {
            case 1 -> now.plus(TIER_1_DURATION);
            case 2 -> now.plus(TIER_2_DURATION);
            case 3 -> now.plus(TIER_3_DURATION);
            default -> throw new IllegalArgumentException("Invalid penalty tier: " + tier);
        };
    }

    /**
     * Scheduled task to decay penalty tiers daily
     */
    @Scheduled(cron = "0 0 12 * * *") // Run daily at noon
    public void scheduledPenaltyDecay() {
        LOGGER.info("Running scheduled penalty decay task");
        processAllPenaltyDecay();
    }

    /**
     * Scheduled task to cleanup expired penalties
     */
    @Scheduled(fixedRate = 60 * 60 * 1000) // Run every hour
    public void scheduledPenaltyCleanup() {
        cleanupExpiredPenalties();
    }

}