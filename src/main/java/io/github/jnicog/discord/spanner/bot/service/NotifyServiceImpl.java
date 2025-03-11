package io.github.jnicog.discord.spanner.bot.service;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotifyServiceImpl implements NotifyService {

    @Autowired
    public NotifyServiceImpl() {
    }

    @Override
    public void notifyPlayerQueuePopped(List<User> queue, MessageChannel channel) {

    }

    @Override
    public void notifyPoppedQueueAccepted(List<User> queue, MessageChannel messageChannel) {

    }

    @Override
    public void notifyPoppedQueueTimeout(List<User> queue, MessageChannel messageChannel) {

    }

    @Override
    public void sendReply(SlashCommandInteractionEvent slashCommandInteractionEvent,
                          String message,
                          boolean isEphemeral) {
        slashCommandInteractionEvent.reply(message).setEphemeral(isEphemeral).queue();
    }

}
