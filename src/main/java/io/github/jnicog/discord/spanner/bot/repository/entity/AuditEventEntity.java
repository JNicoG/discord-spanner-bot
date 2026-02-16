package io.github.jnicog.discord.spanner.bot.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * JPA entity representing an audit event.
 * Stores all events that occur in channels for audit trail purposes.
 */
@Entity
@Table(name = "audit_event")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "channel_id", nullable = false)
    private Long channelId;

    @Column(name = "user_id")
    private Long userId;  // Nullable: some events may not be user-specific

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "event_data", columnDefinition = "jsonb")
    private Map<String, Object> eventData;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private OffsetDateTime occurredAt;

    public AuditEventEntity(Long channelId, Long userId, EventType eventType, Map<String, Object> eventData) {
        this.channelId = channelId;
        this.userId = userId;
        this.eventType = eventType;
        this.eventData = eventData;
        this.occurredAt = OffsetDateTime.now();
    }

    public AuditEventEntity(Long channelId, EventType eventType, Map<String, Object> eventData) {
        this(channelId, null, eventType, eventData);
    }
}

