package io.github.jnicog.discord.spanner.bot.service;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Set;

public interface NotifyService {

    void notifyPlayerQueuePopped(Set<User> queue, MessageChannel channel);

    void notifyPoppedQueueAccepted(Set<User> queue, MessageChannel messageChannel);

    void notifyPoppedQueueTimeout(Set<User> queue, MessageChannel messageChannel);

    void sendReply(SlashCommandInteractionEvent slashCommandInteractionEvent, String message, boolean isEphemeral);

    void sendSilentReply(SlashCommandInteractionEvent slashCommandInteractionEvent, String message);

}
