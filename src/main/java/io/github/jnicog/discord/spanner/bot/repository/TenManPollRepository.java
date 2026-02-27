package io.github.jnicog.discord.spanner.bot.repository;

import io.github.jnicog.discord.spanner.bot.repository.entity.TenManPollEntity;
import io.github.jnicog.discord.spanner.bot.repository.entity.TenManPollStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface TenManPollRepository extends JpaRepository<TenManPollEntity, Long> {

    Optional<TenManPollEntity> findByChannelIdAndStatus(Long channelId, TenManPollStatus status);

    Optional<TenManPollEntity> findByDiscordMessageId(Long discordMessageId);

    List<TenManPollEntity> findByStatusAndClosesAtBefore(TenManPollStatus status, OffsetDateTime now);
}
