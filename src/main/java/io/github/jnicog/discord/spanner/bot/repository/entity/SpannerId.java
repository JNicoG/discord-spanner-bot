package io.github.jnicog.discord.spanner.bot.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Composite primary key for SpannerEntity.
 */
@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SpannerId implements Serializable {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "channel_id", nullable = false)
    private Long channelId;
}

