package io.github.jnicog.discord.spanner.bot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "spanner_votes")
public class SpannerVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private long channelId;

    @Column(nullable = false)
    private long messageId;

    @Column(nullable = false)
    private long targetUserId;

    @Column(nullable = false)
    private long initiatorUserId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false)
    private int yesVotes;

    @Column(nullable = false)
    private int noVotes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoteStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime completedAt;

    public SpannerVote() {
        // Empty constructor
    }

    public SpannerVote(long channelId, long messageId, long targetUserId, long initiatorUserId, String reason) {
        this.channelId = channelId;
        this.messageId = messageId;
        this.targetUserId = targetUserId;
        this.initiatorUserId = initiatorUserId;
        this.reason = reason;
        this.yesVotes = 0;
        this.noVotes = 0;
        this.status = VoteStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public long getChannelId() {
        return channelId;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public long getTargetUserId() {
        return targetUserId;
    }

    public long getInitiatorUserId() {
        return initiatorUserId;
    }

    public String getReason() {
        return reason;
    }

    public int getYesVotes() {
        return yesVotes;
    }

    public void setYesVotes(int yesVotes) {
        this.yesVotes = yesVotes;
    }

    public int getNoVotes() {
        return noVotes;
    }

    public void setNoVotes(int noVotes) {
        this.noVotes = noVotes;
    }

    public VoteStatus getStatus() {
        return status;
    }

    public void setStatus(VoteStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public boolean hasVotePassed() {
        return yesVotes > noVotes;
    }

    public int getTotalVotes() {
        return yesVotes + noVotes;
    }
}