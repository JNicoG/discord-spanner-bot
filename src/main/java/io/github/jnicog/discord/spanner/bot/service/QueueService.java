package io.github.jnicog.discord.spanner.bot.service;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Deque;

/***
 * Service to manage or query the current state of the keener queue.
 */
public interface QueueService {

    void addToQueue(SlashCommandInteractionEvent slashCommandInteractionEvent);

    void removeFromQueue(SlashCommandInteractionEvent slashCommandInteractionEvent);

    Deque<User> getQueue();

    int getQueueSize();

    boolean isQueueFull();

    void setQueueFull();

    void unsetQueueFull();

    void resetQueue();

}
