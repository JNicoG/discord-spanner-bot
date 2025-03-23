package io.github.jnicog.discord.spanner.bot.repository;

import io.github.jnicog.discord.spanner.bot.model.Spanner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpannerRepository extends JpaRepository<Spanner, Integer> {

    Spanner findByUserId(long userId);

    @Query("SELECT s.spannerCount FROM Spanner s WHERE s.userId = :userId")
    Integer getSpannerCount(@Param("userId") long userId);

    @Modifying
    @Query("UPDATE Spanner s SET s.spannerCount = s.spannerCount + 1 WHERE s.userId = :userId")
    void incrementSpannerCount(@Param("userId") long userId);

}
