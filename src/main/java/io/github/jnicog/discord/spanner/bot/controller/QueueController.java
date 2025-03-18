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
import io.github.jnicog.discord.spanner.bot.service.ChannelQueueManager;
import io.github.jnicog.discord.spanner.bot.service.NotificationService;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class QueueController extends ListenerAdapter {
    private static Logger LOGGER = LoggerFactory.getLogger(QueueController.class);

    private final ChannelQueueManager queueManager;
    private final NotificationService notificationService;

    public QueueController(ChannelQueueManager queueManager, NotificationService notificationService) {
        this.queueManager = queueManager;
        this.notificationService = notificationService;
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
            default -> {
                LOGGER.warn("Unknown slash command: {}", event.getName());
                event.reply("Error: Unknown command received").setEphemeral(true).queue();
            }
        }

    }

    private void handleKeenCommand(SlashCommandInteractionEvent event, ChannelQueue queue) {
        User user = event.getUser();

        boolean added = queue.addPlayer(user, event.getMessageChannel());

        if (!added) {
            if (queue.isFull()) {
                notificationService.sendReply(event,
                        "Queue is already full! You cannot join this queue.", true);
            } else {
                notificationService.sendReply(event, "You are already in this queue!", true);
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
            notificationService.sendReply(event, "You are not currently in the queue!", false);
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
                false);

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
                event.reply("Error: unknown button presesd.").setEphemeral(true).queue();
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

        notificationService.sendReply(buttonInteractionEvent, "You have already checked in!", true);
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
                "Check-in for this queue is no longer active!", true);
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
                "You are not a member of this queue!", true);
    }

}
