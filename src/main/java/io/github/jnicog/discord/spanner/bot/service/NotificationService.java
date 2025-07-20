package io.github.jnicog.discord.spanner.bot.service;

import io.github.jnicog.discord.spanner.bot.model.ChannelQueue;
import io.github.jnicog.discord.spanner.bot.model.Spanner;
import io.github.jnicog.discord.spanner.bot.model.SpannerVote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import org.springframework.data.domain.Page;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface NotificationService {

    void sendQueueStatusUpdate(SlashCommandInteractionEvent event, ChannelQueue queue);

    CompletableFuture<Long> sendCheckInStartedMessage(MessageChannel channel, ChannelQueue queue);

    /**
     * For now, only allow check-in accept / decline via button
     * Edits and updates the check-in message for non-terminal user check-ins
     */
    void updateCheckInStatus(MessageChannel channel, ChannelQueue queue, User user);

    void sendReply(IReplyCallback interactionEvent, String message, boolean isEphemeral, boolean mentionRepliedUser, Collection<Message.MentionType> mentionTypes);

    void sendCheckInTimeoutMessage(MessageChannel channel, ChannelQueue queue, Set<User> notCheckedInUsers);

    void sendLeaderboardMessage(IReplyCallback interactionEvent, Page<Spanner> currentLeaderboardList);

    void updateLeaderboardMessage(IReplyCallback interactionEvent, List<Spanner> updatedLeaderboardList);

    CompletableFuture<Long> sendSpannerVotePoll(MessageChannel channel, SpannerVote vote, User targetUser, User initiatorUser);

    void updateSpannerVotePoll(MessageChannel channel, SpannerVote vote, User targetUser);

    void sendVoteCompletedMessage(MessageChannel channel, SpannerVote vote, User targetUser, boolean votePassed);

}
