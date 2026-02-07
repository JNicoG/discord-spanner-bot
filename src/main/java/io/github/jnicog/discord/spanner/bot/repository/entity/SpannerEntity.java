package io.github.jnicog.discord.spanner.bot.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * JPA entity representing a spanner record for a user in a channel.
 */
@Entity
@Table(name = "spanner", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "channel_id"})
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpannerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "channel_id", nullable = false)
    private Long channelId;

    @Column(name = "spanner_count", nullable = false)
    private Integer spannerCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public SpannerEntity(Long userId, Long channelId) {
        this.userId = userId;
        this.channelId = channelId;
        this.spannerCount = 0;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    public void incrementSpannerCount() {
        this.spannerCount++;
        this.updatedAt = OffsetDateTime.now();
    }

    public void incrementSpannerCount(int amount) {
        this.spannerCount += amount;
        this.updatedAt = OffsetDateTime.now();
    }
}

