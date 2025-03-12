package io.github.jnicog.discord.spanner.bot.service;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;
import java.util.Set;

/***
 * Service to manage or query the current state of the keener queue.
 */
public interface QueueService {

    QueueInteractionOutcome joinPlayerQueue(SlashCommandInteractionEvent slashCommandInteractionEvent);

    QueueInteractionOutcome leavePlayerQueue(SlashCommandInteractionEvent slashCommandInteractionEvent);

    List<User> removeUserFromPlayerQueue(List<User> userList);

    void removeUserFromPlayerQueue(User user);

    Set<User> showQueue();

    boolean getQueuePoppedState();

    void resetPlayerQueue();

}
