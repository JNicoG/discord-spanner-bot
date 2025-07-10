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
import org.springframework.data.domain.Page;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;
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
        Long userId = 111111111111111111L;
        Long channelId = 222222222222222222L;
        SpannerId spannerId = new SpannerId(userId, channelId);

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
        Long userId = 111111111111111111L;
        Long channelId = 222222222222222222L;
        SpannerId spannerId = new SpannerId(userId, channelId);

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

    @Test
    public void test_getLeaderboard() {
        // Init database
        Long userId1 = 111111111111111111L;
        Long channelId1 = 111111111111111111L;

        Long userId2 = 222222222222222222L;
        Long channelId2 = 222222222222222222L;

        Long userId3 = 333333333333333333L;

        SpannerId spannerId1 = new SpannerId(userId1, channelId1);
        Spanner spanner1 = new Spanner(userId1, channelId1);
        SpannerId spannerId2 = new SpannerId(userId2, channelId2);
        Spanner spanner2 = new Spanner(userId2, channelId2);
        SpannerId spannerId3 = new SpannerId(userId3, channelId1);
        Spanner spanner3 = new Spanner(userId3, channelId1);

        spanner1.incrementSpannerCount();
        spannerRepository.save(spanner1);
        spannerRepository.save(spanner2);
        spanner3.incrementSpannerCount();
        spanner3.incrementSpannerCount();
        spannerRepository.save(spanner3);

        Assertions.assertEquals(1, spannerService.getSpannerCount(spanner1.getUserId(), spanner1.getChannelId()));
        Assertions.assertEquals(0, spannerService.getSpannerCount(spanner2.getUserId(), spanner2.getChannelId()));
        Assertions.assertEquals(2, spannerService.getSpannerCount(spanner3.getUserId(), spanner3.getChannelId()));

        Assertions.assertTrue(spannerRepository.findById(spannerId1).isPresent());
        Assertions.assertTrue(spannerRepository.findById(spannerId2).isPresent());
        Assertions.assertTrue(spannerRepository.findById(spannerId3).isPresent());

        List<Integer> expectedLeaderboardSpannerCountList = List.of(spanner3.getSpannerCount(), spanner1.getSpannerCount());
        List<Long> expectedLeaderboardUserIdList = List.of(spanner3.getUserId(), spanner1.getUserId());
        List<Long> expectedLeaderboardChannelIdList = List.of(spanner3.getChannelId(), spanner1.getChannelId());
        // Method under test
        Page<Spanner> actualLeaderboardPage = spannerService.getLeaderboard(channelId1, 0, 10);
        List<Spanner> actualLeaderboardList = actualLeaderboardPage.getContent();

        Assertions.assertEquals(expectedLeaderboardSpannerCountList.size(), actualLeaderboardPage.getTotalElements());
        for (int i = 0; i < expectedLeaderboardSpannerCountList.size(); i++) {
            Assertions.assertEquals(expectedLeaderboardSpannerCountList.get(i), actualLeaderboardList.get(i).getSpannerCount());
            Assertions.assertEquals(expectedLeaderboardUserIdList.get(i), actualLeaderboardList.get(i).getUserId());
            Assertions.assertEquals(expectedLeaderboardChannelIdList.get(i), actualLeaderboardList.get(i).getChannelId());
        }
    }

}
