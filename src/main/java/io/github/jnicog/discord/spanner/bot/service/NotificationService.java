package io.github.jnicog.discord.spanner.bot.service;

import io.github.jnicog.discord.spanner.bot.model.ChannelQueue;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface NotificationService {

    void sendQueueStatusUpdate(SlashCommandInteractionEvent event, ChannelQueue queue);

    CompletableFuture<Long> sendCheckInStartedMessage(MessageChannel channel, ChannelQueue queue);

    /**
     * For now, only allow check-in accept / decline via button
     */
    void updateCheckInStatus(MessageChannel channel, ChannelQueue queue, User user);

    void sendReply(IReplyCallback interactionEvent, String message, boolean isEphemeral);

    void sendCheckInTimeoutMessage(MessageChannel channel, ChannelQueue queue, Set<User> notCheckedInUsers);
}
