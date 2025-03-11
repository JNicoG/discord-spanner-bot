package io.github.jnicog.discord.spanner.bot.service;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;

/***
 * Service to manage or query the current state of the keener queue.
 */
public interface QueueService {

    void joinPlayerQueue(SlashCommandInteractionEvent slashCommandInteractionEvent);

    void leavePlayerQueue(SlashCommandInteractionEvent slashCommandInteractionEvent);

    List<User> removeFromPlayerQueue(List<User> removeFromPlayerQueueList);

    List<User> getPlayerQueue();

    boolean isPlayerQueueFull();

    boolean getQueuePoppedState();

    void setQueuePoppedState();

    void unsetQueuePoppedState();

    void resetPlayerQueue();

}
