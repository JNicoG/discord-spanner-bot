package io.github.jnicog.discord.spanner.bot.controller;

import com.google.common.base.Strings;
import io.github.jnicog.discord.spanner.bot.config.QueueProperties;
import io.github.jnicog.discord.spanner.bot.event.CheckInAcceptEvent;
import io.github.jnicog.discord.spanner.bot.event.CheckInAlreadyAcceptedEvent;
import io.github.jnicog.discord.spanner.bot.event.CheckInCancelledEvent;
import io.github.jnicog.discord.spanner.bot.event.CheckInCompletedEvent;
import io.github.jnicog.discord.spanner.bot.event.CheckInOutdatedEvent;
import io.github.jnicog.discord.spanner.bot.event.CheckInStartedEvent;
import io.github.jnicog.discord.spanner.bot.event.CheckInTimeoutEvent;
import io.github.jnicog.discord.spanner.bot.event.NonMemberInteractionEvent;
import io.github.jnicog.discord.spanner.bot.model.ChannelQueue;
import io.github.jnicog.discord.spanner.bot.model.Spanner;
import io.github.jnicog.discord.spanner.bot.service.ChannelQueueManager;
import io.github.jnicog.discord.spanner.bot.service.NotificationService;
import io.github.jnicog.discord.spanner.bot.service.SpannerService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class QueueController extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueueController.class);

    private final ChannelQueueManager queueManager;
    private final NotificationService notificationService;
    private final SpannerService spannerService;

    public QueueController(ChannelQueueManager queueManager,
                           NotificationService notificationService,
                           SpannerService spannerService) {
        this.queueManager = queueManager;
        this.notificationService = notificationService;
        this.spannerService = spannerService;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (Strings.isNullOrEmpty(event.getName())) {
            LOGGER.warn("Slash command received is null or empty");
            return;
        }

        LOGGER.debug("Received slash command: {}", event.getName());

        ChannelQueue queue = queueManager.getOrCreateQueue(event.getChannel());

        switch (event.getName()) {
            case "k", "keen" -> handleKeenCommand(event, queue);
            case "unkeen" -> handleUnkeenCommand(event, queue);
            case "keeners" -> handleKeenersCommand(event, queue);
            case "spanners" -> handleSpannersCommand(event, queue);
            case "leaderboard" -> handleLeaderboardCommand(event, queue);
            default -> {
                LOGGER.warn("Unknown slash command: {}", event.getName());
                event.reply("Error: Unknown command received").setEphemeral(true).queue();
            }
        }
    }

    private void handleLeaderboardCommand(SlashCommandInteractionEvent event, ChannelQueue queue) {
        Page<Spanner> leaderboardPage = spannerService.getLeaderboard(queue.getChannelId(), 0, 5);
        LOGGER.debug("Retrieved {} records: {}", leaderboardPage.getNumberOfElements(), leaderboardPage.getContent());
        notificationService.sendLeaderboardMessage(event, leaderboardPage);
    }

    private void handleSpannersCommand(SlashCommandInteractionEvent event, ChannelQueue queue) {
        // user param input validation already done through Discord API
        OptionMapping optionalUserParam = event.getOption("user");

        User targetUser = optionalUserParam == null ? event.getUser() : optionalUserParam.getAsUser();

        int targetSpannerCount = spannerService.getSpannerCount(targetUser.getIdLong(), queue.getChannelId());

        String replyMessage = String.format("%s has spannered **%d** time%s.",
                targetUser.getAsMention(),
                targetSpannerCount,
                targetSpannerCount == 1 ? "" : "s");

        notificationService.sendReply(event, replyMessage, false, true, Collections.emptyList());

    }

    private void handleKeenCommand(SlashCommandInteractionEvent event, ChannelQueue queue) {
        User user = event.getUser();

        boolean added = queue.addPlayer(user, event.getMessageChannel());

        if (!added) {
            if (queue.isFull()) {
                notificationService.sendReply(event,
                        "Queue is already full! You cannot join this queue.",
                        true,
                        true,
                        EnumSet.of(Message.MentionType.USER));
            } else {
                // Player is already in queue - try to refresh their timeout
                boolean refreshed = queue.refreshPlayer(user);
                if (refreshed) {
                    notificationService.sendReply(event,
                            "You are already in this queue! Your keen has been refreshed.",
                            true,
                            true,
                            EnumSet.of(Message.MentionType.USER));
                } else {
                    notificationService.sendReply(event,
                            "You are already in this queue!",
                            true,
                            true,
                            EnumSet.of(Message.MentionType.USER));
                }
            }
            return;
        }

        notificationService.sendQueueStatusUpdate(event, queue);

    }

    private void handleUnkeenCommand(SlashCommandInteractionEvent event, ChannelQueue queue) {
        User user = event.getUser();

        // queue.removePlayer already publishes a check-in cancel event which will handle the message editing
        boolean removed = queue.removePlayer(user, true);

        if (!removed) {
            notificationService.sendReply(event,
                    "You are not currently in the queue!",
                    true,
                    true,
                    EnumSet.of(Message.MentionType.USER));
            return;
        }

        notificationService.sendQueueStatusUpdate(event, queue);

        // If this is called too soon before other handlers are finished using a queue, this will result in NPEs
        // queueManager.removeQueueIfEmpty(event.getChannel());
    }

    private void handleKeenersCommand(SlashCommandInteractionEvent event, ChannelQueue queue) {
        Set<User> players = queue.getPlayers();
        QueueProperties queueProperties = queue.getQueueProperties();

        String playerList = players.isEmpty() ? "No players in queue"
                : players.stream().map(User::getAsMention).collect(Collectors.joining(", "));

        notificationService.sendReply(event,
                String.format("Current queue [%d/%d]: %s",
                        players.size(),
                        queueProperties.getMaxQueueSize(),
                        playerList),
                false,
                true,
                Collections.emptyList());

    }

    // Handle check-in buttons
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (Strings.isNullOrEmpty(event.getComponentId())) {
            LOGGER.warn("Received button interaction with empty component ID");
            return;
        }

        LOGGER.debug("Handling button interaction: {}", event .getComponentId());

        ChannelQueue queue = queueManager.getOrCreateQueue(event.getChannel());

        switch (event.getComponentId()) {
            case "acceptButton" -> handleAcceptButton(event, queue);
            case "spannerButton" -> handleSpannerButton(event, queue);
            default -> {
                LOGGER.warn("Unknown button interaction: {}", event.getComponentId());
                event.reply("Error: unknown button pressed.").setEphemeral(true).queue();
            }
        }
    }

    private void handleAcceptButton(ButtonInteractionEvent event, ChannelQueue queue) {
        User user = event.getUser();
        long messageId = event.getMessageIdLong();

        if (isInteractionOutdated(queue, user, messageId)) {
            event.reply("This check-in message is no longer active.").setEphemeral(true).queue();
            return;
        }

        if (!isMemberOfQueue(queue, user)) {
            event.reply("You are not a member of this queue!").setEphemeral(true).queue();
            return;
        }

        queue.playerCheckIn(event);

    }

    private void handleSpannerButton(ButtonInteractionEvent event, ChannelQueue queue) {
        User user = event.getUser();
        long messageId = event.getMessageIdLong();

        if (isInteractionOutdated(queue, user, messageId)) {
            event.reply("This check-in message is no longer active.").setEphemeral(true).queue();
            return;
        }

        if (!isMemberOfQueue(queue, user)) {
            event.reply("You are not a member of this queue!").setEphemeral(true).queue();
            return;
        }

        queue.removePlayer(user, true);

    }

    private boolean isInteractionOutdated(ChannelQueue queue, User user, long messageId) {
        if (queue.getLastActiveCheckInMessageId() != messageId || !queue.isFull()) {
            LOGGER.info("User {} interacted with an outdated check-in message", user.getName());
            return true;
        }
        return false;
    }

    private boolean isMemberOfQueue(ChannelQueue queue, User user) {
        if (!queue.getPlayers().contains(user)) {
            LOGGER.info("User {} interacted with a check-in message for a queue they are not a member of",
                    user.getName());
            return false;
        }
        return true;
    }

    @EventListener
    public void handleCheckInAccept(CheckInAcceptEvent event) {
        LOGGER.info("Handling CheckInAcceptEvent for channel {}", event.getChannelId());

        ChannelQueue queue = event.getQueue();
        MessageChannel channel = event.getChannel();

        notificationService.updateCheckInStatus(channel, queue, event.getUser());
    }

    @EventListener
    public void handleCheckInAlreadyAccepted(CheckInAlreadyAcceptedEvent event) {
        LOGGER.info("Handling CheckInAlreadyAccepted for channel {}", event.getChannelId());

        ButtonInteractionEvent buttonInteractionEvent = event.getButtonInteractionEvent();

        notificationService.sendReply(buttonInteractionEvent,
                "You have already checked in!",
                true,
                true,
                EnumSet.of(Message.MentionType.USER));
    }

    @EventListener
    public void handleCheckInCancelled(CheckInCancelledEvent event) {
        LOGGER.info("Handling CheckInCancelledEvent for channel {}", event.getChannelId());

        ChannelQueue queue = event.getQueue();
        MessageChannel channel = event.getChannel();

        notificationService.updateCheckInStatus(channel, queue, event.getUser());
    }

    @EventListener
    public void handleCheckInCompleted(CheckInCompletedEvent event) {
        LOGGER.info("Handling CheckInCompletedEvent for channel {}", event.getChannelId());

        ChannelQueue queue = event.getQueue();
        MessageChannel channel = event.getChannel();

        notificationService.updateCheckInStatus(channel, queue, event.getUser());
        queue.fullReset();
    }

    @EventListener
    public void handleCheckInOutdated(CheckInOutdatedEvent event) {
        LOGGER.info("Handling CheckInOutdatedEvent for channel {}", event.getChannelId());

        notificationService.sendReply(event.getButtonInteractionEvent(),
                "Check-in for this queue is no longer active!",
                true,
                true,
                EnumSet.of(Message.MentionType.USER));
    }

    @EventListener
    public void handleCheckInStarted(CheckInStartedEvent event) {
        LOGGER.info("Handling CheckInStartedEvent for channel {}", event.getChannelId());

        ChannelQueue queue = event.getQueue();
        MessageChannel channel = event.getChannel();

        notificationService.sendCheckInStartedMessage(channel, queue);
        // The notification service already calls queue.setLastActiveCheckInMessageId(messageId); on future completion
    }

    @EventListener
    public void handleCheckInTimeout(CheckInTimeoutEvent event) {
        LOGGER.info("Handling CheckInTimeoutEvent for channel {}", event.getChannelId());

        ChannelQueue queue = event.getQueue();
        MessageChannel channel = event.getChannel();
        Set<User> notCheckedInUsers = event.getNotCheckedInUsers();

        notificationService.sendCheckInTimeoutMessage(channel, queue, notCheckedInUsers);
    }

    @EventListener
    public void handleNonMemberInteractionEvent(NonMemberInteractionEvent event) {
        LOGGER.info("Handling NonMemberInteractionEvent for channel {}", event.getChannelId());

        notificationService.sendReply(event.getButtonInteractionEvent(),
                "You are not a member of this queue!",
                true,
                true,
                EnumSet.of(Message.MentionType.USER));
    }

}
