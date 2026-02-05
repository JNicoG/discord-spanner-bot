package io.github.jnicog.discord.spanner.bot.queue;

import io.github.jnicog.discord.spanner.bot.config.QueueProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Set;

@ExtendWith(SpringExtension.class)
@Import(ChannelQueueManagerTest.ChannelQueueManagerTestConfig.class)
public class ChannelQueueManagerTest {

    private static final int MAX_QUEUE_SIZE = 3;

    private final ChannelQueueManager channelQueueManager;

    QueueProperties queueProperties;

    @Autowired
    ChannelQueueManagerTest(QueueProperties queueProperties) {
        this.queueProperties = queueProperties;
        this.channelQueueManager = new ChannelQueueManager(queueProperties);
    }

    @Test
    void testContext() {
        Assertions.assertNotNull(channelQueueManager);
    }

    @Test
    void test_showQueue_notYetMapped() {
        long textChannelId = 100L;
        Assertions.assertTrue(channelQueueManager.showQueue(textChannelId).isEmpty());
    }

    @Test
    void test_joinQueue() {
        long userId = 1L;
        long textChannelId = 100L;

        QueueOutcome outcome = channelQueueManager.joinQueue(userId, textChannelId);
        Assertions.assertEquals(QueueOutcome.ENQUEUED, outcome);
        Assertions.assertEquals(channelQueueManager.showQueue(textChannelId), Set.of(userId));
    }

    @Test
    void test_joinQueue_untilFull() {
        long textChannelId = 100L;

        for (long userId = 1L; userId <= MAX_QUEUE_SIZE; userId++) {
            QueueOutcome outcome = channelQueueManager.joinQueue(userId, textChannelId);
            Assertions.assertEquals(QueueOutcome.ENQUEUED, outcome);
        }

        // Now the queue should be full
        QueueOutcome outcome = channelQueueManager.joinQueue(MAX_QUEUE_SIZE + 1L, textChannelId);
        Assertions.assertEquals(QueueOutcome.QUEUE_FULL, outcome);
    }

    @Test
    void test_joinQueue_alreadyInQueue() {
        long userId = 1L;
        long textChannelId = 100L;

        QueueOutcome outcome1 = channelQueueManager.joinQueue(userId, textChannelId);
        Assertions.assertEquals(QueueOutcome.ENQUEUED, outcome1);

        QueueOutcome outcome2 = channelQueueManager.joinQueue(userId, textChannelId);
        Assertions.assertEquals(QueueOutcome.ALREADY_QUEUED, outcome2);
    }

    @Test
    void test_joinQueue_alreadyFull() {
        long textChannelId = 100L;
        long userId = 1L;

        for (int i = 0; i < MAX_QUEUE_SIZE; i++) {
            QueueOutcome outcome = channelQueueManager.joinQueue(userId + i, textChannelId);
            Assertions.assertEquals(QueueOutcome.ENQUEUED, outcome);
        }

        QueueOutcome outcome = channelQueueManager.joinQueue(userId + MAX_QUEUE_SIZE, textChannelId);
        Assertions.assertEquals(QueueOutcome.QUEUE_FULL, outcome);
    }

    @Test
    void test_leaveQueue() {
        long userId = 1L;
        long textChannelId = 100L;

        QueueOutcome outcome1 = channelQueueManager.joinQueue(userId, textChannelId);
        Assertions.assertEquals(QueueOutcome.ENQUEUED, outcome1);

        QueueOutcome outcome2 = channelQueueManager.leaveQueue(userId, textChannelId);
        Assertions.assertEquals(QueueOutcome.DEQUEUED, outcome2);
        Assertions.assertTrue(channelQueueManager.showQueue(textChannelId).isEmpty());
    }

    @Test
    void test_leaveQueue_alreadyNotInQueue() {
        long userId = 1L;
        long textChannelId = 100L;

        QueueOutcome outcome = channelQueueManager.leaveQueue(userId, textChannelId);
        Assertions.assertEquals(QueueOutcome.NOT_IN_QUEUE, outcome);
    }

    @Test
    void test_showQueue_empty() {
        long textChannelId = 100L;
        Set<Long> queueSnapshot = channelQueueManager.showQueue(textChannelId);
        Assertions.assertTrue(queueSnapshot.isEmpty());
    }

    @Test
    void test_showQueue_nonEmpty() {
        long textChannelId = 100L;
        long userId1 = 1L;
        long userId2 = 2L;

        channelQueueManager.joinQueue(userId1, textChannelId);
        channelQueueManager.joinQueue(userId2, textChannelId);

        Set<Long> queueSnapshot = channelQueueManager.showQueue(textChannelId);
        Assertions.assertEquals(Set.of(userId1, userId2), queueSnapshot);
    }

    @TestConfiguration
    static class ChannelQueueManagerTestConfig {
        @Bean
        public QueueProperties queueProperties() {
            QueueProperties properties = new QueueProperties();
            properties.setMaxQueueSize(MAX_QUEUE_SIZE);
            return properties;
        }
    }

}
