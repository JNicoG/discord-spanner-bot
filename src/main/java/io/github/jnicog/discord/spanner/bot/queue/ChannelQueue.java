package io.github.jnicog.discord.spanner.bot.queue;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class ChannelQueue {

    private final int maxSize;
    private final Set<Long> currentQueue;
    private final ReentrantLock lock = new ReentrantLock();

    public ChannelQueue(int maxSize) {
        this.maxSize = maxSize;
        this.currentQueue = ConcurrentHashMap.newKeySet(maxSize);
    }

    public QueueOutcome addUserToQueue(long userId) {
        lock.lock();
        try {
            if (isFull()) {
                return QueueOutcome.QUEUE_FULL;
            }
            if (currentQueue.contains(userId)) {
                return QueueOutcome.ALREADY_QUEUED;
            }
            currentQueue.add(userId);
            return QueueOutcome.ENQUEUED;
        } finally {
            lock.unlock();
        }
    }

    public QueueOutcome removeUserFromQueue(long userId) {
        lock.lock();
        try {
            if (!currentQueue.contains(userId)) {
                return QueueOutcome.NOT_IN_QUEUE;
            }
            currentQueue.remove(userId);
            return QueueOutcome.DEQUEUED;
        } finally {
            lock.unlock();
        }
    }

    public boolean isFull() {
        lock.lock();
        try {
            return currentQueue.size() >= maxSize;
        } finally {
            lock.unlock();
        }
    }

    public boolean isEmpty() {
        lock.lock();
        try {
            return currentQueue.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    public Set<Long> snapshot() {
        lock.lock();
        try {
            return Set.copyOf(currentQueue);
        } finally {
            lock.unlock();
        }
    }

    public void clear() {
        lock.lock();
        try {
            currentQueue.clear();
        } finally {
            lock.unlock();
        }
    }

    int getMaxQueueSize() {
        return maxSize;
    }

}
