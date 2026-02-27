package io.github.jnicog.discord.spanner.bot.repository.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ten_man_poll")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TenManPollEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "channel_id", nullable = false)
    private Long channelId;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    @Column(name = "discord_message_id")
    private Long discordMessageId;

    @Column(name = "time_display")
    private String timeDisplay;

    @Column(name = "test_mode", nullable = false)
    private boolean testMode;

    @Column(name = "closes_at", nullable = false)
    private OffsetDateTime closesAt;

    @Column(name = "locked_date_option_id")
    private Long lockedDateOptionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TenManPollStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TenManDateOptionEntity> dateOptions = new ArrayList<>();

    public TenManPollEntity(Long channelId, Long createdByUserId, String timeDisplay, boolean testMode, OffsetDateTime closesAt) {
        this.channelId = channelId;
        this.createdByUserId = createdByUserId;
        this.timeDisplay = timeDisplay;
        this.testMode = testMode;
        this.closesAt = closesAt;
        this.status = TenManPollStatus.ACTIVE;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }
}
