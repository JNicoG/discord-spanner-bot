package io.github.jnicog.discord.spanner.bot.repository;

import io.github.jnicog.discord.spanner.bot.repository.entity.TenManSignupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TenManSignupRepository extends JpaRepository<TenManSignupEntity, Long> {

    List<TenManSignupEntity> findByDateOptionId(Long dateOptionId);

    boolean existsByDateOptionIdAndUserId(Long dateOptionId, Long userId);

    Optional<TenManSignupEntity> findByDateOptionIdAndUserId(Long dateOptionId, Long userId);

    int countByDateOptionId(Long dateOptionId);
}
