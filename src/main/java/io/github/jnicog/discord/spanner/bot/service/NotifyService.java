package io.github.jnicog.discord.spanner.bot.service;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface NotifyService {

    CompletableFuture<Long> notifyPlayerQueuePopped(Map<User, Long> queue, MessageChannel messageChannel);

    void notifyPoppedQueueAccepted(ButtonInteractionEvent buttonInteractionEvent, String message);

    void editPoppedQueueMessage(MessageChannel messageChannel, long activeQueueMessageId, String message);

    void notifyPoppedQueuePlayerAccept(ButtonInteractionEvent buttonInteractionEvent, String message);

    void sendReply(IReplyCallback genericEvent, String message, boolean isEphemeral);

    void sendSilentReply(IReplyCallback genericEvent, String message);

}
