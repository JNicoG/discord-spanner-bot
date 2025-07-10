package io.github.jnicog.discord.spanner.bot.service;

import io.github.jnicog.discord.spanner.bot.TestConfig;
import io.github.jnicog.discord.spanner.bot.model.Spanner;
import io.github.jnicog.discord.spanner.bot.model.SpannerId;
import io.github.jnicog.discord.spanner.bot.repository.SpannerRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Optional;

@SpringBootTest(classes = {TestConfig.class})
public class SpannerServiceImplTest {

    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:17");

    @BeforeAll
    static void beforeAll() {
        postgreSQLContainer.start();
    }

    @AfterAll
    static void afterAll() {
        postgreSQLContainer.stop();
    }

	@DynamicPropertySource
	static void dynamicProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
		registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
		registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
	}

    @Autowired
    SpannerService spannerService;

    @Autowired
    SpannerRepository spannerRepository;

    // getSpannerCount() method currently does NOT SAVE to repository if user_id is not yet existent
    // i.e. calling getSpannerCount() on user 123456789L when there is no initial entry in the database will
    // NOT result in the creation of a record for this user.
    // TODO: Consider adding a command to 'register' a user for the first time
    @Test
    public void shouldInitialiseNewSpannerWithZeroCount() {
        // user_id under test
        Long userId = 123456789L;
        Long channelId = 987654321L;
        SpannerId spannerId = new SpannerId(userId.intValue(), channelId.intValue());

        // initial assertions
        Optional<Spanner> spanner  = spannerRepository.findById(spannerId);
        Assertions.assertEquals(Optional.empty(), spanner);

        // method under test
        int actualCount = spannerService.getSpannerCount(userId, channelId);
        Optional<Spanner> updatedSpanner = spannerRepository.findById(spannerId);

        Assertions.assertEquals(0, actualCount);
        Assertions.assertTrue(updatedSpanner.isEmpty());
    }

    @Test
    public void shouldIncrementCount() {
        Long userId = 123456789L;
        Long channelId = 987654321L;
        SpannerId spannerId = new SpannerId(userId.intValue(), channelId.intValue());

        int actualCount = spannerService.getSpannerCount(userId, channelId);
        Assertions.assertEquals(0, actualCount);
        Optional<Spanner> spanner  = spannerRepository.findById(spannerId);
        Assertions.assertEquals(Optional.empty(), spanner);

        spannerService.incrementSpannerCount(userId, channelId);
        actualCount = spannerService.getSpannerCount(userId, channelId);
        Assertions.assertEquals(1, actualCount);

        Optional<Spanner> updatedSpanner = spannerRepository.findById(spannerId);
        Assertions.assertTrue(updatedSpanner.isPresent());
        Assertions.assertEquals(1, spannerService.getSpannerCount(userId, channelId));
    }

}
