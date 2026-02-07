package io.github.jnicog.discord.spanner.bot.repository;

import io.github.jnicog.discord.spanner.bot.TestConfig;
import io.github.jnicog.discord.spanner.bot.TestContainersConfig;
import io.github.jnicog.discord.spanner.bot.repository.entity.SpannerEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import({TestContainersConfig.class, TestConfig.class})
@ActiveProfiles("test")
class SpannerRepositoryTest {

    @Autowired
    private SpannerRepository spannerRepository;

    private static final Long USER_ID_1 = 123456789L;
    private static final Long USER_ID_2 = 987654321L;
    private static final Long CHANNEL_ID_1 = 111111111L;
    private static final Long CHANNEL_ID_2 = 222222222L;

    @BeforeEach
    void setUp() {
        spannerRepository.deleteAll();
    }

    @Test
    void shouldSaveAndFindSpannerEntity() {
        SpannerEntity entity = new SpannerEntity(USER_ID_1, CHANNEL_ID_1);
        spannerRepository.save(entity);

        Optional<SpannerEntity> found = spannerRepository.findByUserIdAndChannelId(USER_ID_1, CHANNEL_ID_1);

        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(USER_ID_1);
        assertThat(found.get().getChannelId()).isEqualTo(CHANNEL_ID_1);
        assertThat(found.get().getSpannerCount()).isZero();
    }

    @Test
    void shouldIncrementSpannerCount() {
        SpannerEntity entity = new SpannerEntity(USER_ID_1, CHANNEL_ID_1);
        spannerRepository.save(entity);

        entity.incrementSpannerCount();
        spannerRepository.save(entity);

        Optional<SpannerEntity> found = spannerRepository.findByUserIdAndChannelId(USER_ID_1, CHANNEL_ID_1);

        assertThat(found).isPresent();
        assertThat(found.get().getSpannerCount()).isEqualTo(1);
    }

    @Test
    void shouldGetSpannerCountByUserIdAndChannelId() {
        SpannerEntity entity = new SpannerEntity(USER_ID_1, CHANNEL_ID_1);
        entity.incrementSpannerCount(5);
        spannerRepository.save(entity);

        Optional<Integer> count = spannerRepository.getSpannerCountByUserIdAndChannelId(USER_ID_1, CHANNEL_ID_1);

        assertThat(count).isPresent();
        assertThat(count.get()).isEqualTo(5);
    }

    @Test
    void shouldReturnEmptyWhenNoSpannerRecord() {
        Optional<Integer> count = spannerRepository.getSpannerCountByUserIdAndChannelId(USER_ID_1, CHANNEL_ID_1);

        assertThat(count).isEmpty();
    }

    @Test
    void shouldGetTotalSpannerCountAcrossChannels() {
        SpannerEntity entity1 = new SpannerEntity(USER_ID_1, CHANNEL_ID_1);
        entity1.incrementSpannerCount(3);
        spannerRepository.save(entity1);

        SpannerEntity entity2 = new SpannerEntity(USER_ID_1, CHANNEL_ID_2);
        entity2.incrementSpannerCount(7);
        spannerRepository.save(entity2);

        int totalCount = spannerRepository.getTotalSpannerCountByUserId(USER_ID_1);

        assertThat(totalCount).isEqualTo(10);
    }

    @Test
    void shouldFindAllByUserId() {
        SpannerEntity entity1 = new SpannerEntity(USER_ID_1, CHANNEL_ID_1);
        SpannerEntity entity2 = new SpannerEntity(USER_ID_1, CHANNEL_ID_2);
        SpannerEntity entity3 = new SpannerEntity(USER_ID_2, CHANNEL_ID_1);
        spannerRepository.save(entity1);
        spannerRepository.save(entity2);
        spannerRepository.save(entity3);

        var userEntities = spannerRepository.findByUserId(USER_ID_1);

        assertThat(userEntities).hasSize(2);
    }

    @Test
    void shouldFindAllByChannelId() {
        SpannerEntity entity1 = new SpannerEntity(USER_ID_1, CHANNEL_ID_1);
        SpannerEntity entity2 = new SpannerEntity(USER_ID_2, CHANNEL_ID_1);
        SpannerEntity entity3 = new SpannerEntity(USER_ID_1, CHANNEL_ID_2);
        spannerRepository.save(entity1);
        spannerRepository.save(entity2);
        spannerRepository.save(entity3);

        var channelEntities = spannerRepository.findByChannelId(CHANNEL_ID_1);

        assertThat(channelEntities).hasSize(2);
    }

    @Test
    void shouldCheckExistsByUserIdAndChannelId() {
        SpannerEntity entity = new SpannerEntity(USER_ID_1, CHANNEL_ID_1);
        spannerRepository.save(entity);

        assertThat(spannerRepository.existsByUserIdAndChannelId(USER_ID_1, CHANNEL_ID_1)).isTrue();
        assertThat(spannerRepository.existsByUserIdAndChannelId(USER_ID_2, CHANNEL_ID_1)).isFalse();
    }
}

