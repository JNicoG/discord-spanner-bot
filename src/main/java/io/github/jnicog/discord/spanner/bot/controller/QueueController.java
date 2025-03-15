package io.github.jnicog.discord.spanner.bot.controller;

import com.google.common.base.Strings;
import io.github.jnicog.discord.spanner.bot.model.ChannelQueue;
import io.github.jnicog.discord.spanner.bot.service.ChannelQueueManager;
import io.github.jnicog.discord.spanner.bot.service.NotificationService;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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

        boolean duringCheckIn = queue.isCheckInActive();
        boolean removed = queue.removePlayer(user, true);

        if (!removed) {
            notificationService.sendReply(event, "You are not currently in the queue!", false);
            return;
        }

        // queue.removePlayer publishes a check-in cancel event which will handle the message edit during check-in
        if (!duringCheckIn) {
            notificationService.sendQueueStatusUpdate(event, queue);
        }

        queueManager.removeQueueIfEmpty(event.getChannel());
    }

    private void handleKeenersCommand(SlashCommandInteractionEvent event, ChannelQueue queue) {

    }



}
