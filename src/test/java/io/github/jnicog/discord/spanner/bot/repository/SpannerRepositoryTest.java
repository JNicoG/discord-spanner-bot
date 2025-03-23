package io.github.jnicog.discord.spanner.bot.repository;

import io.github.jnicog.discord.spanner.bot.model.Spanner;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.utility.TestcontainersConfiguration;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class SpannerRepositoryTest {

    @Autowired
    private SpannerRepository spannerRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    public void shouldSaveAndRetrieveSpanner() {
        // Given
        long userId = 123456789L;
        Spanner spanner = new Spanner(userId);

        // When
        spannerRepository.save(spanner);
        Spanner retrieved = spannerRepository.findByUserId(userId);

        // Then
        Assertions.assertNotNull(retrieved);
        Assertions.assertEquals(userId, retrieved.getUserId());
        Assertions.assertEquals(0, retrieved.getSpannerCount());
    }

    @Test
    public void shouldIncrementSpannerCount() {
        // Given
        long userId = 987654321L;
        Spanner spanner = new Spanner(userId);
        spannerRepository.save(spanner);

        // When
        spannerRepository.incrementSpannerCount(userId);
        entityManager.clear();
        Spanner retrieved = spannerRepository.findByUserId(userId);

        // Then
        Assertions.assertNotNull(retrieved);
        Assertions.assertEquals(userId, retrieved.getUserId());
        Assertions.assertEquals(1, retrieved.getSpannerCount());

    }

}
