package io.github.jnicog.discord.spanner.bot.queue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class ChannelQueueTest {

    private static final int QUEUE_CAPACITY = 5;
    private final ChannelQueue channelQueue;

    public ChannelQueueTest() {
        this.channelQueue = new ChannelQueue(QUEUE_CAPACITY);
    }

    @Test
    void test_AddUser() {
        long userId = 1234567890L;

        QueueOutcome outcome = channelQueue.addUserToQueue(userId);

        Assertions.assertEquals(QueueOutcome.ENQUEUED, outcome);
        Assertions.assertTrue(channelQueue.snapshot().contains(userId));
    }

    @Test
    void test_AddUser_QueueAlreadyFull() {
        long i = 0L;
        for (; i < QUEUE_CAPACITY; i++) {
            channelQueue.addUserToQueue(i);
        }

        QueueOutcome outcome = channelQueue.addUserToQueue(i+1);

        Assertions.assertEquals(QueueOutcome.QUEUE_FULL, outcome);
    }

    @Test
    void test_AddUser_AlreadyInQueue() {
        long userId = 1234567890L;
        QueueOutcome outcome = channelQueue.addUserToQueue(userId);
        Assertions.assertEquals(QueueOutcome.ENQUEUED, outcome);
        Assertions.assertTrue(channelQueue.snapshot().contains(userId));

        QueueOutcome actualOutcome = channelQueue.addUserToQueue(userId);

        Assertions.assertEquals(QueueOutcome.ALREADY_QUEUED, actualOutcome);
    }

    @Test
    void test_AddUserAndFillQueue() {
        ArrayList<Long> users = new ArrayList<>();
        long i = 0L;
        for (; i < QUEUE_CAPACITY; i++) {
            users.add(i);
            channelQueue.addUserToQueue(i);
        }
        Assertions.assertTrue(channelQueue.isFull());
        Assertions.assertTrue(channelQueue.snapshot().containsAll(users));
    }

    @Test
    void test_RemoveUser() {
        long userId = 1234567890L;
        QueueOutcome outcome = channelQueue.addUserToQueue(userId);
        Assertions.assertTrue(channelQueue.snapshot().contains(userId));
        Assertions.assertEquals(QueueOutcome.ENQUEUED, outcome);

        QueueOutcome actualOutcome = channelQueue.removeUserFromQueue(userId);

        Assertions.assertEquals(QueueOutcome.DEQUEUED, actualOutcome);
        Assertions.assertFalse(channelQueue.snapshot().contains(userId));
    }

    @Test
    void test_RemoveUser_NotInQueue() {
        Long userId = 1234567890L;
        Assertions.assertTrue(channelQueue.isEmpty());
        Assertions.assertFalse(channelQueue.snapshot().contains(userId));

        QueueOutcome actualOutcome = channelQueue.removeUserFromQueue(userId);

        Assertions.assertEquals(QueueOutcome.NOT_IN_QUEUE, actualOutcome);
        Assertions.assertTrue(channelQueue.isEmpty());
        Assertions.assertFalse(channelQueue.snapshot().contains(userId));
    }

}
