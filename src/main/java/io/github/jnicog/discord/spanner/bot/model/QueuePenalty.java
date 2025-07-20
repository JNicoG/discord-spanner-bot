package io.github.jnicog.discord.spanner.bot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "queue_penalties")
@IdClass(SpannerId.class)
public class QueuePenalty {

    @Id
    private long userId;

    @Id
    private long channelId;

    @Column(nullable = false)
    private int currentTier = 0;

    @Column(nullable = false)
    private Instant lastPenaltyTime;

    @Column(nullable = false)
    private Instant cooldownExpiresAt;

    public QueuePenalty() {
        // Empty constructor for JPA
    }

    public QueuePenalty(long userId, long channelId) {
        this.userId = userId;
        this.channelId = channelId;
        this.currentTier = 0;
        this.lastPenaltyTime = Instant.now();
        this.cooldownExpiresAt = Instant.now();
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getChannelId() {
        return channelId;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }

    public int getCurrentTier() {
        return currentTier;
    }

    public void setCurrentTier(int currentTier) {
        this.currentTier = currentTier;
    }

    public Instant getLastPenaltyTime() {
        return lastPenaltyTime;
    }

    public void setLastPenaltyTime(Instant lastPenaltyTime) {
        this.lastPenaltyTime = lastPenaltyTime;
    }

    public Instant getCooldownExpiresAt() {
        return cooldownExpiresAt;
    }

    public void setCooldownExpiresAt(Instant cooldownExpiresAt) {
        this.cooldownExpiresAt = cooldownExpiresAt;
    }

    /**
     * Checks if the user is currently under cooldown
     */
    public boolean isUnderCooldown() {
        return Instant.now().isBefore(cooldownExpiresAt);
    }

    /**
     * Gets the remaining cooldown duration in milliseconds
     */
    public long getRemainingCooldownMillis() {
        if (!isUnderCooldown()) {
            return 0;
        }
        return cooldownExpiresAt.toEpochMilli() - Instant.now().toEpochMilli();
    }

}