package io.github.jnicog.discord.spanner.bot.repository;

import io.github.jnicog.discord.spanner.bot.repository.entity.TenManDateOptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TenManDateOptionRepository extends JpaRepository<TenManDateOptionEntity, Long> {

    List<TenManDateOptionEntity> findByPollIdOrderByOptionDateAsc(Long pollId);
}
