package io.github.jnicog.discord.spanner.bot.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * JPA entity representing a spanner record for a user in a channel.
 * Uses a composite primary key of (user_id, channel_id).
 */
@Entity
@Table(name = "spanner")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpannerEntity {

    @EmbeddedId
    private SpannerId id;

    @Column(name = "spanner_count", nullable = false)
    private Integer spannerCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public SpannerEntity(Long userId, Long channelId) {
        this.id = new SpannerId(userId, channelId);
        this.spannerCount = 0;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getUserId() {
        return id.getUserId();
    }

    public Long getChannelId() {
        return id.getChannelId();
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

