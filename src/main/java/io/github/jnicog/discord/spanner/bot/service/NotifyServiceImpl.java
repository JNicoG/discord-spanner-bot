package io.github.jnicog.discord.spanner.bot.service;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

@Service
public class NotifyServiceImpl implements NotifyService {

    @Autowired
    public NotifyServiceImpl() {
        // Empty constructor
    }

    @Override
    public void notifyPlayerQueuePopped(Set<User> queue, MessageChannel channel) {

    }

    @Override
    public void notifyPoppedQueueAccepted(Set<User> queue, MessageChannel messageChannel) {

    }

    @Override
    public void notifyPoppedQueueDeclined(MessageChannel messageChannel, String message) {
        messageChannel.sendMessage(message)
                .setAllowedMentions(Collections.emptyList())
                .queue();
    }

    @Override
    public void notifyPoppedQueueTimeout(Set<User> queue, MessageChannel messageChannel) {

    }

    @Override
    public void sendReply(SlashCommandInteractionEvent slashCommandInteractionEvent, String message, boolean isEphemeral) {
        slashCommandInteractionEvent.reply(message)
                .setEphemeral(isEphemeral)
                .queue();
    }

    @Override
    public void sendSilentReply(SlashCommandInteractionEvent slashCommandInteractionEvent, String message) {
        slashCommandInteractionEvent.reply(message)
                .setEphemeral(false)
                .setAllowedMentions(Collections.emptyList())
                .queue();
    }

}
