package io.github.jnicog.discord.spanner.bot.service;

import io.github.jnicog.discord.spanner.bot.config.QueueProperties;
import io.github.jnicog.discord.spanner.bot.model.ChannelQueue;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final QueueProperties queueProperties;

    public NotificationServiceImpl(QueueProperties queueProperties) {
        this.queueProperties = queueProperties;
    }

    @Override
    public void sendQueueStatusUpdate(SlashCommandInteractionEvent event, ChannelQueue queue) {
        String action = event.getName().equals("unkeen") ? "left" : "joined";

        event.deferReply().queue();
        event.getHook().editOriginal(
                String.format("%s has %s the queue! [%d/%d]\nCurrent queue: %s",
                        event.getUser().getAsMention(),
                        action,
                        queue.getPlayers().size(),
                        queueProperties.getMaxQueueSize(),
                        queue.getPlayers().stream().map(User::getAsMention).collect(Collectors.joining(" "))
                )
        ).mentionRepliedUser(true).queue();
    }

    @Override
    public void sendQueueStatusUpdate(MessageChannel channel, ChannelQueue queue) {

    }

    @Override
    public CompletableFuture<Long> sendCheckInStartedMessage(MessageChannel channel, ChannelQueue queue) {
        return null;
    }

    @Override
    public void updateCheckInStatus(ButtonInteractionEvent event, ChannelQueue queue) {

    }

    @Override
    public void sendReply(IReplyCallback interactionEvent, String message, boolean isEphemeral) {
        interactionEvent.deferReply().queue();
        interactionEvent.getHook().editOriginal(message).queue();
    }

    @Override
    public void editMessage(MessageChannel channel, long messageId, String content) {

    }
}
