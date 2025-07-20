package io.github.jnicog.discord.spanner.bot.service;

import io.github.jnicog.discord.spanner.bot.TestConfig;
import io.github.jnicog.discord.spanner.bot.model.QueuePenalty;
import io.github.jnicog.discord.spanner.bot.model.SpannerId;
import io.github.jnicog.discord.spanner.bot.repository.QueuePenaltyRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {TestConfig.class})
public class QueuePenaltyServiceImplTest {

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
    private QueuePenaltyService queuePenaltyService;

    @Autowired
    private QueuePenaltyRepository queuePenaltyRepository;

    private final long testUserId = 123456789L;
    private final long testChannelId = 987654321L;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        queuePenaltyRepository.deleteAll();
    }

    @Test
    void shouldNotHaveCooldownForNewUser() {
        assertFalse(queuePenaltyService.isUserUnderCooldown(testUserId, testChannelId));
        assertEquals(Duration.ZERO, queuePenaltyService.getRemainingCooldown(testUserId, testChannelId));
    }

    @Test
    void shouldApplyTier1PenaltyForFirstOffense() {
        int tier = queuePenaltyService.applyPenalty(testUserId, testChannelId);
        
        assertEquals(1, tier);
        assertTrue(queuePenaltyService.isUserUnderCooldown(testUserId, testChannelId));
        
        Duration remainingCooldown = queuePenaltyService.getRemainingCooldown(testUserId, testChannelId);
        assertTrue(remainingCooldown.getSeconds() > 50 && remainingCooldown.getSeconds() <= 60);
    }

    @Test
    void shouldEscalatePenaltyTierForRecentOffenses() {
        // First penalty
        int tier1 = queuePenaltyService.applyPenalty(testUserId, testChannelId);
        assertEquals(1, tier1);
        
        // Second penalty immediately after
        int tier2 = queuePenaltyService.applyPenalty(testUserId, testChannelId);
        assertEquals(2, tier2);
        
        // Third penalty
        int tier3 = queuePenaltyService.applyPenalty(testUserId, testChannelId);
        assertEquals(3, tier3);
        
        // Fourth penalty should still be tier 3 (max tier)
        int tier4 = queuePenaltyService.applyPenalty(testUserId, testChannelId);
        assertEquals(3, tier4);
    }

    @Test
    void shouldResetToTier1AfterOneDayWithoutPenalties() {
        // Apply first penalty
        queuePenaltyService.applyPenalty(testUserId, testChannelId);
        
        // Manually adjust the last penalty time to be more than 24 hours ago
        Optional<QueuePenalty> penaltyOpt = queuePenaltyService.getPenaltyInfo(testUserId, testChannelId);
        assertTrue(penaltyOpt.isPresent());
        
        QueuePenalty penalty = penaltyOpt.get();
        penalty.setLastPenaltyTime(Instant.now().minus(25, ChronoUnit.HOURS));
        penalty.setCooldownExpiresAt(Instant.now().minus(24, ChronoUnit.HOURS)); // Make cooldown expired
        queuePenaltyRepository.save(penalty);
        
        // Apply new penalty should reset to tier 1
        int newTier = queuePenaltyService.applyPenalty(testUserId, testChannelId);
        assertEquals(1, newTier);
    }

    @Test
    void shouldNotAllowActionWhenUnderCooldown() {
        // Apply penalty to put user under cooldown
        queuePenaltyService.applyPenalty(testUserId, testChannelId);
        
        assertTrue(queuePenaltyService.isUserUnderCooldown(testUserId, testChannelId));
        assertTrue(queuePenaltyService.getRemainingCooldown(testUserId, testChannelId).toSeconds() > 0);
    }

    @Test
    void shouldDecayPenaltyTiers() {
        // Create a penalty that's eligible for decay (tier > 0, cooldown expired, last penalty > 24h ago)
        QueuePenalty penalty = new QueuePenalty(testUserId, testChannelId);
        penalty.setCurrentTier(2);
        penalty.setLastPenaltyTime(Instant.now().minus(25, ChronoUnit.HOURS));
        penalty.setCooldownExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));
        queuePenaltyRepository.save(penalty);
        
        // Process decay
        queuePenaltyService.processAllPenaltyDecay();
        
        // Check that tier was decremented
        Optional<QueuePenalty> updatedPenaltyOpt = queuePenaltyService.getPenaltyInfo(testUserId, testChannelId);
        assertTrue(updatedPenaltyOpt.isPresent());
        assertEquals(1, updatedPenaltyOpt.get().getCurrentTier());
    }

    @Test
    void shouldCleanupExpiredTier0Penalties() {
        // Create an expired penalty with tier 0
        QueuePenalty penalty = new QueuePenalty(testUserId, testChannelId);
        penalty.setCurrentTier(0);
        penalty.setCooldownExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));
        queuePenaltyRepository.save(penalty);
        
        // Verify penalty exists
        assertTrue(queuePenaltyService.getPenaltyInfo(testUserId, testChannelId).isPresent());
        
        // Cleanup expired penalties
        queuePenaltyService.cleanupExpiredPenalties();
        
        // Verify penalty was deleted
        assertFalse(queuePenaltyService.getPenaltyInfo(testUserId, testChannelId).isPresent());
    }

    @Test
    void shouldHandleMultipleChannelsIndependently() {
        long channelId2 = 555555555L;
        
        // Apply penalty in first channel
        int tier1 = queuePenaltyService.applyPenalty(testUserId, testChannelId);
        assertEquals(1, tier1);
        
        // Apply penalty in second channel should also be tier 1
        int tier2 = queuePenaltyService.applyPenalty(testUserId, channelId2);
        assertEquals(1, tier2);
        
        // Both channels should have independent penalties
        assertTrue(queuePenaltyService.isUserUnderCooldown(testUserId, testChannelId));
        assertTrue(queuePenaltyService.isUserUnderCooldown(testUserId, channelId2));
    }

    @Test
    void shouldGetPenaltyInfoCorrectly() {
        // No penalty initially
        assertTrue(queuePenaltyService.getPenaltyInfo(testUserId, testChannelId).isEmpty());
        
        // Apply penalty
        queuePenaltyService.applyPenalty(testUserId, testChannelId);
        
        // Get penalty info
        Optional<QueuePenalty> penaltyOpt = queuePenaltyService.getPenaltyInfo(testUserId, testChannelId);
        assertTrue(penaltyOpt.isPresent());
        
        QueuePenalty penalty = penaltyOpt.get();
        assertEquals(testUserId, penalty.getUserId());
        assertEquals(testChannelId, penalty.getChannelId());
        assertEquals(1, penalty.getCurrentTier());
        assertTrue(penalty.isUnderCooldown());
    }
}