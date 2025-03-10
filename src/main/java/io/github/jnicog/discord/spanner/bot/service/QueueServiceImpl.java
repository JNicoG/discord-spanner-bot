package io.github.jnicog.discord.spanner.bot.service;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;

@Service
public class QueueServiceImpl implements QueueService {

    private static final int MAX_QUEUE_SIZE = 5;
    private final Deque<User> queue = new ArrayDeque<>(MAX_QUEUE_SIZE);
    private static boolean isQueueFull = false;

    @Autowired
    private final NotifyService notifyService;

    public QueueServiceImpl(NotifyService notifyService) {
        this.notifyService = notifyService;
    }

    /***
     * Slash commands occur asynchronously whereas the queue is shared
     * among all threads. Need synchronized keyword to prevent race conditions
     * where multiple threads manipulate the queue simultaneously.
     */
    @Override
    public synchronized void addToQueue(SlashCommandInteractionEvent slashCommandInteractionEvent) {
        User user = slashCommandInteractionEvent.getUser();

        if (queue.contains(user)) {
            slashCommandInteractionEvent.reply(QueueStatus.ALREADY_IN_QUEUE.description).setEphemeral(true).queue();
            return;
        }

        if (queue.size() >= MAX_QUEUE_SIZE) {
            slashCommandInteractionEvent.reply(QueueStatus.QUEUE_FULL.description).setEphemeral(true).queue();
            return;
        }

        queue.add(user);
        slashCommandInteractionEvent.reply(
                String.format("%s%s [%d/5]",
                        user.getAsMention(),
                        QueueStatus.ADDED_TO_QUEUE.description,
                        queue.size()))
                .queue();

        if (queue.size() == MAX_QUEUE_SIZE && !isQueueFull) {
            setQueueFull();
            notifyService.notifyQueueReady(queue, slashCommandInteractionEvent.getChannel());
        }

    }

    @Override
    public void removeFromQueue(SlashCommandInteractionEvent slashCommandInteractionEvent) {
        User user = slashCommandInteractionEvent.getUser();

        if (!queue.contains(user)) {
            slashCommandInteractionEvent.reply(QueueStatus.NOT_IN_QUEUE.description).setEphemeral(true).queue();
        }

        queue.remove(slashCommandInteractionEvent.getUser());
        slashCommandInteractionEvent.reply(
                String.format(
                        "%s%s [%d/5]",
                        user.getAsMention(),
                        QueueStatus.REMOVED_FROM_QUEUE.description,
                        queue.size()))
                .queue();
    }

    @Override
    public Deque<User> getQueue() {
        return queue;
    }

    @Override
    public int getQueueSize() {
        return queue.size();
    }

    @Override
    public boolean isQueueFull() {
        return isQueueFull;
    }

    @Override
    public void setQueueFull() {
        isQueueFull = true;
    }

    @Override
    public void unsetQueueFull() {
        isQueueFull = false;
    }

    @Override
    public void resetQueue() {
        queue.clear();
        unsetQueueFull();
    }

}
