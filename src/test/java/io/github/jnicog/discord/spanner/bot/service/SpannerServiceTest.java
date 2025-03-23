package io.github.jnicog.discord.spanner.bot.service;

import io.github.jnicog.discord.spanner.bot.model.Spanner;
import io.github.jnicog.discord.spanner.bot.repository.SpannerRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.utility.TestcontainersConfiguration;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
public class SpannerServiceTest {

    @Autowired
    private SpannerService spannerService;

    @Autowired
    private SpannerRepository spannerRepository;

    @Test
    public void shouldCreateNewSpannerWhenIncrementingNonExistentUser() {
        // Given
        long userId = 12345678L;

        // When
        spannerService.incrementSpannerCount(userId);

        // Then
        Spanner spanner = spannerRepository.findByUserId(userId);
        Assertions.assertNotNull(spanner);
        Assertions.assertEquals(1, spanner.getSpannerCount());
    }

    @Test
    public void shouldIncrementExistingSpanner() {
        // Given
        long userId = 123456789L;
        Spanner spanner = new Spanner(userId);
        spannerRepository.save(spanner);
        Spanner existingSpanner = spannerRepository.findByUserId(userId);
        Assertions.assertNotNull(existingSpanner);
        Assertions.assertEquals(0, existingSpanner.getSpannerCount());

        // When
        spannerService.incrementSpannerCount(userId);

        // Then
        int count = spannerService.getSpannerCount(userId);
        Assertions.assertEquals(1, count);

    }

    @Test
    public void shouldReturnZeroForNonExistentUser() {
        // Given
        long userId = 987654321L;

        // When
        int count = spannerService.getSpannerCount(userId);

        // Then
        Assertions.assertEquals(0, count);

    }

}
