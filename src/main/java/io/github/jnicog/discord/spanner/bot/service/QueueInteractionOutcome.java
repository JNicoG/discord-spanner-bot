package io.github.jnicog.discord.spanner.bot.service;

public enum QueueInteractionOutcome {
    QUEUE_ALREADY_FULL("Queue is already full!"),
    ALREADY_IN_QUEUE("You are already in the queue!"),
    ADDED_TO_QUEUE(" has been added to the queue!"),
    REMOVED_FROM_QUEUE(" has left the queue!"),
    ALREADY_NOT_IN_QUEUE("You are currently not in the queue!");

    private final String description;

    QueueInteractionOutcome(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}
