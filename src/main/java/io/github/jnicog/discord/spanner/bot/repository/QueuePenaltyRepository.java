package io.github.jnicog.discord.spanner.bot.repository;

import io.github.jnicog.discord.spanner.bot.model.QueuePenalty;
import io.github.jnicog.discord.spanner.bot.model.SpannerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface QueuePenaltyRepository extends JpaRepository<QueuePenalty, SpannerId> {

    /**
     * Find penalties that are eligible for tier decay (cooldown has expired and last penalty is more than 24 hours ago)
     */
    @Query("SELECT qp FROM QueuePenalty qp WHERE qp.cooldownExpiresAt < :now AND qp.lastPenaltyTime < :dayAgo AND qp.currentTier > 0")
    List<QueuePenalty> findPenaltiesEligibleForDecay(@Param("now") Instant now, @Param("dayAgo") Instant dayAgo);

    /**
     * Find all penalties that should be cleaned up (tier 0 and cooldown expired)
     */
    @Query("SELECT qp FROM QueuePenalty qp WHERE qp.currentTier = 0 AND qp.cooldownExpiresAt < :now")
    List<QueuePenalty> findPenaltiesToCleanup(@Param("now") Instant now);

}