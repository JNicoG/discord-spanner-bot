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

    List<User> removeUserFromPlayerQueue(List<User> userList);

    void removeUserFromPlayerQueue(User user);

    void showQueue(SlashCommandInteractionEvent slashCommandInteractionEvent);

    void resetPlayerQueue();

}
