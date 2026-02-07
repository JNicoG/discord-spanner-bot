package io.github.jnicog.discord.spanner.bot.repository;

import io.github.jnicog.discord.spanner.bot.repository.entity.AuditEventEntity;
import io.github.jnicog.discord.spanner.bot.repository.entity.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * JPA repository for AuditEventEntity operations.
 */
@Repository
public interface AuditEventRepository extends JpaRepository<AuditEventEntity, Long> {

    /**
     * Finds all events for a channel, ordered by occurrence time descending.
     */
    List<AuditEventEntity> findByChannelIdOrderByOccurredAtDesc(Long channelId);

    /**
     * Finds all events for a channel with pagination.
     */
    Page<AuditEventEntity> findByChannelIdOrderByOccurredAtDesc(Long channelId, Pageable pageable);

    /**
     * Finds all events for a user across all channels.
     */
    List<AuditEventEntity> findByUserIdOrderByOccurredAtDesc(Long userId);

    /**
     * Finds all events of a specific type in a channel.
     */
    List<AuditEventEntity> findByChannelIdAndEventTypeOrderByOccurredAtDesc(Long channelId, EventType eventType);

    /**
     * Finds all events for a user in a specific channel.
     */
    List<AuditEventEntity> findByChannelIdAndUserIdOrderByOccurredAtDesc(Long channelId, Long userId);

    /**
     * Finds events in a channel within a time range.
     */
    @Query("SELECT e FROM AuditEventEntity e WHERE e.channelId = :channelId " +
           "AND e.occurredAt BETWEEN :startTime AND :endTime ORDER BY e.occurredAt DESC")
    List<AuditEventEntity> findByChannelIdAndTimeRange(
            @Param("channelId") Long channelId,
            @Param("startTime") OffsetDateTime startTime,
            @Param("endTime") OffsetDateTime endTime);

    /**
     * Counts events of a specific type for a user in a channel.
     */
    long countByChannelIdAndUserIdAndEventType(Long channelId, Long userId, EventType eventType);

    /**
     * Finds the most recent events in a channel.
     */
    List<AuditEventEntity> findTop10ByChannelIdOrderByOccurredAtDesc(Long channelId);
}

