package io.github.jnicog.discord.spanner.bot.spanner;

import io.github.jnicog.discord.spanner.bot.TestConfig;
import io.github.jnicog.discord.spanner.bot.TestContainersConfig;
import io.github.jnicog.discord.spanner.bot.repository.SpannerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import({TestContainersConfig.class, TestConfig.class})
@ActiveProfiles("test")
class SpannerServiceIntegrationTest {

    @Autowired
    private SpannerService spannerService;

    @Autowired
    private SpannerRepository spannerRepository;

    private static final long USER_ID = 123456789L;
    private static final long CHANNEL_ID = 111111111L;

    @BeforeEach
    void setUp() {
        spannerRepository.deleteAll();
    }

    @Test
    void shouldIncrementSpannerCountForNewUser() {
        spannerService.incrementSpannerCount(USER_ID, CHANNEL_ID);

        int count = spannerService.getSpannerCount(USER_ID, CHANNEL_ID);

        assertEquals(1, count);
    }

    @Test
    void shouldIncrementSpannerCountForExistingUser() {
        spannerService.incrementSpannerCount(USER_ID, CHANNEL_ID);
        spannerService.incrementSpannerCount(USER_ID, CHANNEL_ID);
        spannerService.incrementSpannerCount(USER_ID, CHANNEL_ID);

        int count = spannerService.getSpannerCount(USER_ID, CHANNEL_ID);

        assertEquals(3, count);
    }

    @Test
    void shouldReturnZeroForUserWithNoSpanners() {
        int count = spannerService.getSpannerCount(USER_ID, CHANNEL_ID);

        assertEquals(0, count);
    }

    @Test
    void shouldGetTotalSpannerCountAcrossChannels() {
        long channelId2 = 222222222L;

        spannerService.incrementSpannerCount(USER_ID, CHANNEL_ID);
        spannerService.incrementSpannerCount(USER_ID, CHANNEL_ID);
        spannerService.incrementSpannerCount(USER_ID, channelId2);
        spannerService.incrementSpannerCount(USER_ID, channelId2);
        spannerService.incrementSpannerCount(USER_ID, channelId2);

        int totalCount = spannerService.getTotalSpannerCount(USER_ID);

        assertEquals(5, totalCount);
    }
}

