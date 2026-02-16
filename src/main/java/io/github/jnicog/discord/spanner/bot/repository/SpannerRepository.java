package io.github.jnicog.discord.spanner.bot.repository;

import io.github.jnicog.discord.spanner.bot.repository.entity.SpannerEntity;
import io.github.jnicog.discord.spanner.bot.repository.entity.SpannerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository for SpannerEntity operations.
 * Uses composite primary key (user_id, channel_id).
 */
@Repository
public interface SpannerRepository extends JpaRepository<SpannerEntity, SpannerId> {

    /**
     * Finds a spanner record by user ID and channel ID.
     */
    default Optional<SpannerEntity> findByUserIdAndChannelId(Long userId, Long channelId) {
        return findById(new SpannerId(userId, channelId));
    }

    /**
     * Finds all spanner records for a user across all channels.
     */
    @Query("SELECT s FROM SpannerEntity s WHERE s.id.userId = :userId")
    List<SpannerEntity> findByUserId(@Param("userId") Long userId);

    /**
     * Finds all spanner records for a channel.
     */
    @Query("SELECT s FROM SpannerEntity s WHERE s.id.channelId = :channelId")
    List<SpannerEntity> findByChannelId(@Param("channelId") Long channelId);

    /**
     * Checks if a spanner record exists for a user in a channel.
     */
    default boolean existsByUserIdAndChannelId(Long userId, Long channelId) {
        return existsById(new SpannerId(userId, channelId));
    }

    /**
     * Gets the total spanner count for a user across all channels.
     */
    @Query("SELECT COALESCE(SUM(s.spannerCount), 0) FROM SpannerEntity s WHERE s.id.userId = :userId")
    int getTotalSpannerCountByUserId(@Param("userId") Long userId);

    /**
     * Gets the spanner count for a user in a specific channel.
     */
    @Query("SELECT s.spannerCount FROM SpannerEntity s WHERE s.id.userId = :userId AND s.id.channelId = :channelId")
    Optional<Integer> getSpannerCountByUserIdAndChannelId(@Param("userId") Long userId, @Param("channelId") Long channelId);
}
