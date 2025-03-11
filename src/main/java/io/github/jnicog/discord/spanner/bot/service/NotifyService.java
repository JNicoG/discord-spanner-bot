package io.github.jnicog.discord.spanner.bot.service;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;

public interface NotifyService {

    void notifyPlayerQueuePopped(List<User> queue, MessageChannel channel);

    void notifyPoppedQueueAccepted(List<User> queue, MessageChannel messageChannel);

    void notifyPoppedQueueTimeout(List<User> queue, MessageChannel messageChannel);

    void sendReply(SlashCommandInteractionEvent slashCommandInteractionEvent, String message, boolean isEphemeral);

}
