package io.github.jnicog.discord.spanner.bot.repository;

import io.github.jnicog.discord.spanner.bot.model.Spanner;
/*import org.springframework.data.jpa.repository.JpaRepository;*/

public interface SpannerRepository /*extends JpaRepository<Spanner, Integer>*/ {
    Spanner findByUserId(long userId);

    int getSpannerCount(long userId);

    void incrementSpannerCount(long userId);

}
