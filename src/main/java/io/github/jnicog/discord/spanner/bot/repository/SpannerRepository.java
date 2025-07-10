package io.github.jnicog.discord.spanner.bot.repository;

import io.github.jnicog.discord.spanner.bot.model.Spanner;
import io.github.jnicog.discord.spanner.bot.model.SpannerId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpannerRepository extends JpaRepository<Spanner, SpannerId> {

    Page<Spanner> findByChannelIdOrderBySpannerCountDesc(long channelId, Pageable pageable);

}
