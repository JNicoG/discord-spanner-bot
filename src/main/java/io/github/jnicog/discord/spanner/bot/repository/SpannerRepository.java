package io.github.jnicog.discord.spanner.bot.repository;

import io.github.jnicog.discord.spanner.bot.repository.entity.SpannerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository for SpannerEntity operations.
 */
@Repository
public interface SpannerRepository extends JpaRepository<SpannerEntity, Long> {

    /**
     * Finds a spanner record by user ID and channel ID.
     */
    Optional<SpannerEntity> findByUserIdAndChannelId(Long userId, Long channelId);

    /**
     * Finds all spanner records for a user across all channels.
     */
    List<SpannerEntity> findByUserId(Long userId);

    /**
     * Finds all spanner records for a channel.
     */
    List<SpannerEntity> findByChannelId(Long channelId);

    /**
     * Checks if a spanner record exists for a user in a channel.
     */
    boolean existsByUserIdAndChannelId(Long userId, Long channelId);


    /**
     * Gets the total spanner count for a user across all channels.
     */
    @Query("SELECT COALESCE(SUM(s.spannerCount), 0) FROM SpannerEntity s WHERE s.userId = :userId")
    int getTotalSpannerCountByUserId(@Param("userId") Long userId);

    /**
     * Gets the spanner count for a user in a specific channel.
     */
    @Query("SELECT COALESCE(s.spannerCount, 0) FROM SpannerEntity s WHERE s.userId = :userId AND s.channelId = :channelId")
    Optional<Integer> getSpannerCountByUserIdAndChannelId(@Param("userId") Long userId, @Param("channelId") Long channelId);
}

