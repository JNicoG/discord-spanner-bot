package io.github.jnicog.discord.spanner.bot.service;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static io.github.jnicog.discord.spanner.bot.service.QueueInteractionOutcome.ADDED_TO_QUEUE;
import static io.github.jnicog.discord.spanner.bot.service.QueueInteractionOutcome.ALREADY_IN_QUEUE;
import static io.github.jnicog.discord.spanner.bot.service.QueueInteractionOutcome.ALREADY_NOT_IN_QUEUE;
import static io.github.jnicog.discord.spanner.bot.service.QueueInteractionOutcome.QUEUE_ALREADY_FULL;
import static io.github.jnicog.discord.spanner.bot.service.QueueInteractionOutcome.REMOVED_FROM_QUEUE;

@Service
public class QueueServiceImpl implements QueueService {

    private static final int MAX_QUEUE_SIZE = 5;
    private final List<User> playerQueue = new ArrayList<>(MAX_QUEUE_SIZE);
    private static boolean isQueuePoppedState = false;

    @Autowired
    private final NotifyService notifyService;

    public QueueServiceImpl(NotifyService notifyService) {
        this.notifyService = notifyService;
    }

    @Override
    public void joinPlayerQueue(SlashCommandInteractionEvent slashCommandInteractionEvent) {
        User user = slashCommandInteractionEvent.getUser();

        if (playerQueue.contains(user)) {
            notifyService.sendReply(slashCommandInteractionEvent, ALREADY_IN_QUEUE.getDescription(), true);
            return;
        }

        if (playerQueue.size() >= MAX_QUEUE_SIZE) {
            notifyService.sendReply(slashCommandInteractionEvent, QUEUE_ALREADY_FULL.getDescription(), true);
            return;
        }

        playerQueue.add(user);
        notifyService.sendReply(slashCommandInteractionEvent,
                String.format("%s%s [%d/5]", user.getAsMention(), ADDED_TO_QUEUE.getDescription(), playerQueue.size()),
                false);

        if (isPlayerQueueFull() && !getQueuePoppedState()) {
            setQueuePoppedState();
            notifyService.notifyPlayerQueuePopped(playerQueue, slashCommandInteractionEvent.getMessageChannel());
        }
    }

    @Override
    public void leavePlayerQueue(SlashCommandInteractionEvent slashCommandInteractionEvent) {
        User user = slashCommandInteractionEvent.getUser();

        if (!playerQueue.contains(user)) {
            notifyService.sendReply(slashCommandInteractionEvent, ALREADY_NOT_IN_QUEUE.getDescription(), true);
            return;
        }

        playerQueue.remove(slashCommandInteractionEvent.getUser());
        notifyService.sendReply(slashCommandInteractionEvent,
                String.format("%s%s [%d/5]",
                        user.getAsMention(),
                        REMOVED_FROM_QUEUE.getDescription(),
                        playerQueue.size()),
                false);

        if (getQueuePoppedState()) {
            unsetQueuePoppedState();
        }
    }

    @Override
    public List<User> removeFromPlayerQueue(List<User> removeFromPlayerQueueList) {
        List<User> playersRemovedFromQueue = new ArrayList<>(5);

        for (User user : removeFromPlayerQueueList) {
            if (!playerQueue.remove(user)) {
                throw new IllegalArgumentException(
                        String.format("Invalid user list provided. " +
                                        "Failed to remove user %s from player queue " +
                                        "(specified user is not in the player queue)",
                                user.getIdLong()));
            }
            playersRemovedFromQueue.add(user);
        }
        return playersRemovedFromQueue;
    }

    @Override
    public List<User> getPlayerQueue() {
        return playerQueue;
    }

    @Override
    public boolean isPlayerQueueFull() {
        return getPlayerQueue().size() == MAX_QUEUE_SIZE;
    }

    @Override
    public boolean getQueuePoppedState() {
        return isQueuePoppedState;
    }

    @Override
    public void setQueuePoppedState() {
        isQueuePoppedState = true;
    }

    @Override
    public void unsetQueuePoppedState() {
        isQueuePoppedState = false;
    }

    @Override
    public void resetPlayerQueue() {
        playerQueue.clear();
        unsetQueuePoppedState();
    }

}
