package io.github.jnicog.discord.spanner.bot.queue;

public enum QueueOutcome {
    // Join queue
    ENQUEUED,
    ALREADY_QUEUED,
    QUEUE_FULL,

    // Leave queue
    DEQUEUED,
    NOT_IN_QUEUE
}
