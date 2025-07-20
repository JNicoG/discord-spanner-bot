package io.github.jnicog.discord.spanner.bot.repository;

import io.github.jnicog.discord.spanner.bot.model.SpannerVote;
import io.github.jnicog.discord.spanner.bot.model.VoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpannerVoteRepository extends JpaRepository<SpannerVote, Long> {

    Optional<SpannerVote> findByMessageIdAndStatus(long messageId, VoteStatus status);

    List<SpannerVote> findByStatusAndCreatedAtBefore(VoteStatus status, LocalDateTime cutoffTime);

    List<SpannerVote> findByChannelIdOrderByCreatedAtDesc(long channelId);
}