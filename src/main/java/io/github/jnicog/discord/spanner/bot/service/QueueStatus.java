package io.github.jnicog.discord.spanner.bot.service;

public enum QueueStatus {
    QUEUE_FULL("The queue is already full!"),
    ALREADY_IN_QUEUE("You are already in the queue!"),
    ADDED_TO_QUEUE(" has joined the queue!"),
    REMOVED_FROM_QUEUE(" has left the queue!"),
    NOT_IN_QUEUE("You are currently not in the queue!");

    public final String description;

    QueueStatus(String description) {
        this.description = description;
    }
}
