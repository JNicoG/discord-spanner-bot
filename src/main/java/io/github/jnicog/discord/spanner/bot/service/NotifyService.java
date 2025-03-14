package io.github.jnicog.discord.spanner.bot.service;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public interface NotifyService {

    CompletableFuture<Long> notifyPlayerQueuePopped(Map<User, KeenMetadata> queue, MessageChannel channel);

    void notifyPoppedQueueAccepted(Set<User> queue, MessageChannel messageChannel);

    void notifyPoppedQueueDeclined(GenericEvent genericEvent, String message);

    void notifyPoppedQueueTimeout(Set<User> queue, MessageChannel messageChannel);

    void notifyPoppedQueuePlayerAccept(ButtonInteractionEvent buttonInteractionEvent, String message);

    void sendReply(IReplyCallback genericEvent, String message, boolean isEphemeral);

    void sendSilentReply(IReplyCallback genericEvent, String message);

}
