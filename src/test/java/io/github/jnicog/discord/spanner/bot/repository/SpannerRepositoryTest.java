package io.github.jnicog.discord.spanner.bot.repository;

import io.github.jnicog.discord.spanner.bot.AbstractIntegrationTest;
import io.github.jnicog.discord.spanner.bot.repository.entity.SpannerEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpannerRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private SpannerRepository spannerRepository;

    private static final Long USER_ID_1 = 123456789L;
    private static final Long USER_ID_2 = 987654321L;
    private static final Long CHANNEL_ID_1 = 111111111L;
    private static final Long CHANNEL_ID_2 = 222222222L;

    @Test
    void shouldSaveAndFindSpannerEntity() {
        SpannerEntity entity = new SpannerEntity(USER_ID_1, CHANNEL_ID_1);
        spannerRepository.save(entity);

        Optional<SpannerEntity> found = spannerRepository.findByUserIdAndChannelId(USER_ID_1, CHANNEL_ID_1);

        assertTrue(found.isPresent());
        assertEquals(USER_ID_1, found.get().getUserId());
        assertEquals(CHANNEL_ID_1, found.get().getChannelId());
        assertEquals(0, found.get().getSpannerCount());
    }

    @Test
    void shouldIncrementSpannerCount() {
        SpannerEntity entity = new SpannerEntity(USER_ID_1, CHANNEL_ID_1);
        spannerRepository.save(entity);

        entity.incrementSpannerCount();
        spannerRepository.save(entity);

        Optional<SpannerEntity> found = spannerRepository.findByUserIdAndChannelId(USER_ID_1, CHANNEL_ID_1);

        assertTrue(found.isPresent());
        assertEquals(1, found.get().getSpannerCount());
    }

    @Test
    void shouldGetSpannerCountByUserIdAndChannelId() {
        SpannerEntity entity = new SpannerEntity(USER_ID_1, CHANNEL_ID_1);
        entity.incrementSpannerCount(5);
        spannerRepository.save(entity);

        Optional<Integer> count = spannerRepository.getSpannerCountByUserIdAndChannelId(USER_ID_1, CHANNEL_ID_1);

        assertTrue(count.isPresent());
        assertEquals(5, count.get());
    }

    @Test
    void shouldReturnEmptyWhenNoSpannerRecord() {
        Optional<Integer> count = spannerRepository.getSpannerCountByUserIdAndChannelId(USER_ID_1, CHANNEL_ID_1);

        assertTrue(count.isEmpty());
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

        assertEquals(10, totalCount);
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

        assertEquals(2, userEntities.size());
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

        assertEquals(2, channelEntities.size());
    }

    @Test
    void shouldCheckExistsByUserIdAndChannelId() {
        SpannerEntity entity = new SpannerEntity(USER_ID_1, CHANNEL_ID_1);
        spannerRepository.save(entity);

        assertTrue(spannerRepository.existsByUserIdAndChannelId(USER_ID_1, CHANNEL_ID_1));
        assertFalse(spannerRepository.existsByUserIdAndChannelId(USER_ID_2, CHANNEL_ID_1));
    }
}
