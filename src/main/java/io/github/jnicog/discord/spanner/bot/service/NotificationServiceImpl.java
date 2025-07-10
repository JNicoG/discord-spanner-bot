package io.github.jnicog.discord.spanner.bot.service;

import io.github.jnicog.discord.spanner.bot.config.QueueProperties;
import io.github.jnicog.discord.spanner.bot.model.ChannelQueue;
import io.github.jnicog.discord.spanner.bot.model.Spanner;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static io.github.jnicog.discord.spanner.bot.service.Constants.acceptButton;
import static io.github.jnicog.discord.spanner.bot.service.Constants.awaitingButton;
import static io.github.jnicog.discord.spanner.bot.service.Constants.checkMarkEmoji;
import static io.github.jnicog.discord.spanner.bot.service.Constants.spannerButton;

@Service
public class NotificationServiceImpl implements NotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImpl.class);

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
                        queue.isEmpty() ? "No players in queue"
                        : queue.getPlayers().stream().map(User::getAsMention).collect(Collectors.joining(" "))
                )
        ).mentionRepliedUser(true).queue();
    }

    @Override
    public CompletableFuture<Long> sendCheckInStartedMessage(MessageChannel channel, ChannelQueue queue) {
        CompletableFuture<Long> futureMessageId = new CompletableFuture<>();

        String playerStatusList = formatPlayerCheckInList(queue.getCheckInStatusMap());

        String checkInMessage = buildCheckInMessage(playerStatusList, false);

        channel.sendMessage(checkInMessage)
                .addActionRow(acceptButton, spannerButton)
                .queue(
                        sentMessage -> {
                            long messageId = sentMessage.getIdLong();
                            LOGGER.info("Sent check-in message with ID {} to channel {}",
                                    messageId, queue.getChannelId());

                            queue.setLastActiveCheckInMessageId(messageId);

                            futureMessageId.complete(messageId);
                        },
                        error -> {
                            LOGGER.error("Failed to send check-in message: {}", error.getMessage());
                            futureMessageId.completeExceptionally(error);
                        }
                );

        return futureMessageId;
    }

    private String buildCheckInMessage(String playerStatusList, boolean checkInComplete) {
        if (!checkInComplete) {
            return String.format("The queue has been filled!" +
                            "\nClick the %s button within %d %s to accept." +
                            "\nWaiting for all players to accept..." +
                            "\n%s",
                    checkMarkEmoji,
                    queueProperties.getCheckInTimeoutLength(),
                    queueProperties.getCheckInTimeoutUnit().toString().toLowerCase(),
                    playerStatusList);
        }
        return String.format("All players have accepted!" +
                "\n%s",
                playerStatusList);
    }

    private String formatPlayerCheckInList(Map<User, Boolean> checkInStatus) {
        if (checkInStatus.isEmpty()) {
            return "No players in check-in";
        }

        return checkInStatus.entrySet().stream()
                .map(entry -> {
                    User user = entry.getKey();
                    boolean hasCheckedIn = entry.getValue();
                    return String.format("%s [%s]",
                            user.getAsMention(), hasCheckedIn ? checkMarkEmoji : awaitingButton);
                })
                .collect(Collectors.joining(" | "));
    }

    @Override
    public void updateCheckInStatus(MessageChannel channel, ChannelQueue queue, User user) {
        String playerStatusList = formatPlayerCheckInList(queue.getCheckInStatusMap());
        // Might need a condition below to check what to do if check-in player count is zero
        boolean checkInComplete = queue.getCheckInStatusMap().values()
                .stream()
                .allMatch(Boolean::booleanValue);

        String message = queue.isFull() ? buildCheckInMessage(playerStatusList, checkInComplete)
                : buildCheckInCancelledMessage(queue, user);

        LOGGER.debug("Built check-in status update message: {}", message);

        LOGGER.info("Editing check-in message with id {} in channel {}",
                queue.getLastActiveCheckInMessageId(), channel);

        MessageEditAction messageEditAction = channel.editMessageById(queue.getLastActiveCheckInMessageId(), message);

        if (!queue.isFull() || (queue.isFull() && checkInComplete)) {
            messageEditAction.setComponents(Collections.emptyList());
        } else {
            messageEditAction.setActionRow(acceptButton, spannerButton);
        }
        messageEditAction.queue(success -> {
            LOGGER.debug("Check-in status message successfully edited");
        }, error -> {
            LOGGER.error("Failed to update check-in status message: {}", error.getMessage());
        });
    }

    private String buildCheckInCancelledMessage(ChannelQueue queue, User user) {
        LOGGER.info("Check-in cancelled by {} with remaining players: {}", user.getName(),
                queue.getPlayers().stream().map(User::getAsMention).collect(Collectors.joining(", ")));
        if (queue.getPlayers().isEmpty()) {
            return String.format("Check-in cancelled by %s\n" +
                    "No players remaining in queue.",
                    user.getAsMention());
        }
        return String.format("Check-in cancelled by %s\n" +
                        "The following remaining players will be returned to the queue: %s",
                user.getAsMention(),
                queue.getPlayers().stream().map(User::getAsMention).collect(Collectors.joining(", "))
        );
    }

    private String buildCheckInTimedOutMessage(ChannelQueue queue, Set<User> notCheckedInUsers) {
        return String.format("Check-in has been cancelled.\n" +
                        "The following players did not check-in on time: %s\n" +
                        "The following remaining players will be returned to the queue: %s",
                notCheckedInUsers.stream().map(User::getAsMention).collect(Collectors.joining(", ")),
                queue.getPlayers().stream().map(User::getAsMention).collect(Collectors.joining(", "))
        );
    }

    @Override
    public void sendReply(IReplyCallback interactionEvent,
                          String message,
                          boolean isEphemeral,
                          boolean mentionRepliedUser,
                          Collection<Message.MentionType> mentionTypes) {

        interactionEvent.deferReply()
                .setEphemeral(isEphemeral)
                .mentionRepliedUser(mentionRepliedUser)
                .setAllowedMentions(mentionTypes)
                .queue(success -> {
                    LOGGER.debug("Successfully deferred reply after interaction");
        }, error -> {
            LOGGER.error("Failed to defer reply after interaction: {}", error.getMessage());
        });
        interactionEvent.getHook()
                .setEphemeral(isEphemeral)
                .editOriginal(message)
                .mentionRepliedUser(mentionRepliedUser)
                .setAllowedMentions(mentionTypes)
                .queue(success -> {
                    LOGGER.debug("Sent reply successfully with isEphemeral={}", isEphemeral);
        }, error -> {
            LOGGER.error("Failed to edit original message: {}", error.getMessage());
        });
    }

    @Override
    public void sendCheckInTimeoutMessage(MessageChannel channel, ChannelQueue queue, Set<User> notCheckedInUsers) {
        String message = buildCheckInTimedOutMessage(queue, notCheckedInUsers);
        channel.editMessageById(queue.getLastActiveCheckInMessageId(), message).setComponents(Collections.emptyList())
                .queue();
    }

    @Override
    public void sendLeaderboardMessage(IReplyCallback interactionEvent, Page<Spanner> leaderboardPage) {
        Collection<Long> userIds = leaderboardPage.getContent().stream()
                .map(Spanner::getUserId)
                .collect(Collectors.toList());
        LOGGER.debug("Leaderboard page userIds: {}", userIds);

        Objects.requireNonNull(interactionEvent.getGuild()).retrieveMembersByIds(userIds).onSuccess(members -> {
            LOGGER.debug("Successfully retrieved members list: {}", members);
            Map<Long, String> memberMentions = members.stream()
                    .collect(Collectors.toMap(member -> member.getUser().getIdLong(), Member::getAsMention));

            LOGGER.debug("Mapping member userIds to user mentions: {}", memberMentions);
            String leaderboardMessage = buildLeaderboardMessage(leaderboardPage, memberMentions);

            sendReply(interactionEvent, leaderboardMessage, false, true, Collections.emptySet());

        }).onError(error -> {
            sendReply(interactionEvent, "Failed to retrieve user data.", false, true, Collections.emptyList());
            LOGGER.error("Failed to retrieve members for leaderboard: {}", error.getMessage());
        });
    }

    private String buildLeaderboardMessage(Page<Spanner> leaderboardPage, Map<Long, String> memberEffectiveNamesMap) {
        StringBuilder leaderboardMessage = new StringBuilder("**Spanner Leaderboard**\n");
        List<Spanner> pageList = leaderboardPage.getContent();
        if (leaderboardPage.isEmpty()) {
            leaderboardMessage.append("The leaderboard is empty.");
        } else {
            for (int i = 0; i < pageList.size(); i++) {
                Spanner spanner = pageList.get(i);
                int rank = (leaderboardPage.getNumber() * leaderboardPage.getSize()) + i + 1;
                leaderboardMessage.append(
                        String.format("%d. **%s**: **%d** spanner%s\n",
                                rank,
                                memberEffectiveNamesMap.getOrDefault(spanner.getUserId(), "Unknown user"),
                                spanner.getSpannerCount(),
                                spanner.getSpannerCount() == 1 ? "" : "s"));
            }
        }
        return leaderboardMessage.toString();
    }

    @Override
    public void updateLeaderboardMessage(IReplyCallback interactionEvent, List<Spanner> updatedLeaderboardList) {
        // TODO: Implement pagination
    }

}
