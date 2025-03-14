package io.github.jnicog.discord.spanner.bot.controller;

import com.google.common.base.Strings;
import io.github.jnicog.discord.spanner.bot.repository.SpannerRepository;
import io.github.jnicog.discord.spanner.bot.service.AcceptService;
import io.github.jnicog.discord.spanner.bot.service.NotifyService;
import io.github.jnicog.discord.spanner.bot.service.QueueInteractionOutcome;
import io.github.jnicog.discord.spanner.bot.service.QueueService;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static io.github.jnicog.discord.spanner.bot.service.QueueInteractionOutcome.ADDED_TO_QUEUE;
import static io.github.jnicog.discord.spanner.bot.service.QueueInteractionOutcome.ALREADY_IN_QUEUE;
import static io.github.jnicog.discord.spanner.bot.service.QueueInteractionOutcome.ALREADY_NOT_IN_QUEUE;
import static io.github.jnicog.discord.spanner.bot.service.QueueInteractionOutcome.QUEUE_ALREADY_FULL;
import static io.github.jnicog.discord.spanner.bot.service.QueueInteractionOutcome.REMOVED_FROM_QUEUE;
import static io.github.jnicog.discord.spanner.bot.service.QueueServiceImpl.MAX_QUEUE_SIZE;

@Component
public class QueueCommandHandler extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueCommandHandler.class);

    private final QueueService queueService;
    private final NotifyService notifyService;
    private final SpannerRepository spannerRepository;
    private final AcceptService acceptService;

    @Autowired
    public QueueCommandHandler(QueueService queueService,
                               NotifyService notifyService,
                               SpannerRepository spannerRepository,
                               AcceptService acceptService) {
        this.queueService = queueService;
        this.notifyService = notifyService;
        this.spannerRepository = spannerRepository;
        this.acceptService = acceptService;
    }

    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent buttonInteractionEvent) {
        if (!Strings.isNullOrEmpty(buttonInteractionEvent.getComponentId())) {
            routeButtonInteraction(buttonInteractionEvent);
        } else {
            throw new RuntimeException("Invalid button pressed. Button not recognised.");
        }
    }

    private void routeButtonInteraction(ButtonInteractionEvent buttonInteractionEvent) {
        switch (buttonInteractionEvent.getComponentId()) {
            case "spannerButton" -> handleSpannerButton(buttonInteractionEvent);
            case "acceptButton" -> handleAcceptButton(buttonInteractionEvent);
            default -> handleInvalidButton();
        }
    }

    private void handleAcceptButton(ButtonInteractionEvent buttonInteractionEvent) {
        User user = buttonInteractionEvent.getUser();
        long messageId = buttonInteractionEvent.getMessageIdLong();

        if (!acceptService.isActiveQueueMessage(messageId)) {
            buttonInteractionEvent.reply("This queue is no longer active.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if (!queueService.getPlayerQueue().containsKey(user)) {
            buttonInteractionEvent.reply("You are not in the current queue!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if (!queueService.isPlayerQueueFull()) {
            buttonInteractionEvent.reply("There is no active queue to accept right now.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        try {
            acceptService.playerAccept(buttonInteractionEvent);
        } catch (Exception e) {
            LOGGER.error("Error processing accept button: {}", e.getMessage(), e);
            buttonInteractionEvent.reply("Something went wrong while processing your request. Please try again.")
                    .setEphemeral(true)
                    .queue();
        }
    }

    private void handleSpannerButton(ButtonInteractionEvent buttonInteractionEvent) {
        User user = buttonInteractionEvent.getUser();
        long messageId = buttonInteractionEvent.getMessageIdLong();

        if (!acceptService.isActiveQueueMessage(messageId)) {
            buttonInteractionEvent.reply("This queue is no longer active.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if (!queueService.getPlayerQueue().containsKey(user)) {
            buttonInteractionEvent.reply("You are not in the current queue!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Consider adding accept service cancelTimeout here
        queueService.removeUserFromPlayerQueue(user);
        spannerRepository.incrementSpannerCount(user.getIdLong());
        notifyService.editPoppedQueueMessage(buttonInteractionEvent.getMessageChannel(),
                acceptService.getActiveQueueMessageId(),
                String.format("%s has spannered. The remaining keeners will be returned to the queue.",
                        buttonInteractionEvent.getUser().getAsMention()));
        acceptService.cancelActiveQueue(); // Consider splitting timeoutTask cancel from setMessageId, set Id here
    }

    private void handleInvalidButton() {
        throw new RuntimeException("Unknown button ID.");
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent slashCommandInteractionEvent) {
        if (!Strings.isNullOrEmpty(slashCommandInteractionEvent.getName())) {
            routeSlashCommand(slashCommandInteractionEvent);
        } else {
            throw new RuntimeException("Invalid slash command.");
        }
    }

    private void routeSlashCommand(SlashCommandInteractionEvent slashCommandInteractionEvent) {
        switch (slashCommandInteractionEvent.getName()) {
            case "k", "keen" -> handleKeen(slashCommandInteractionEvent);
            case "unkeen" -> handleUnkeen(slashCommandInteractionEvent);
            case "keeners" -> handleKeeners(slashCommandInteractionEvent);
            default -> handleInvalidCommand();
        }
    }

    private void handleKeen(SlashCommandInteractionEvent slashCommandInteractionEvent) {
        QueueInteractionOutcome queueInteractionOutcome = queueService.joinPlayerQueue(slashCommandInteractionEvent);

        switch (queueInteractionOutcome) {
            case ALREADY_IN_QUEUE -> notifyService.sendReply(
                    slashCommandInteractionEvent, ALREADY_IN_QUEUE.getDescription(), true);
            case QUEUE_ALREADY_FULL -> notifyService.sendReply(
                    slashCommandInteractionEvent, QUEUE_ALREADY_FULL.getDescription(), true);
            case ADDED_TO_QUEUE -> notifyService.sendSilentReply(
                    slashCommandInteractionEvent,
                    String.format("%s%s\nCurrent queue: [%d/%d]\n%s",
                            slashCommandInteractionEvent.getUser().getAsMention(),
                            ADDED_TO_QUEUE.getDescription(),
                            queueService.showQueue().size(),
                            MAX_QUEUE_SIZE,
                            queueService.showQueue().stream()
                                    .map(User::getAsMention)
                                    .collect(Collectors.joining(" "))));
        }

        if (queueInteractionOutcome.equals(ADDED_TO_QUEUE)
                && queueService.isPlayerQueueFull()) {

            LOGGER.info("Queue is full, popping queue with {} members", queueService.showQueue());

            MessageChannel messageChannel = slashCommandInteractionEvent.getMessageChannel();

            CompletableFuture<Long> queuePopMessage = notifyService.notifyPlayerQueuePopped(
                    queueService.getPlayerQueue(), messageChannel);

            queuePopMessage.thenAccept(messageId -> {
                LOGGER.info("Queue pop message sent with ID: {}, initializing acceptance tracking", messageId);

                acceptService.initialiseAcceptQueue(messageId, messageChannel);
            }).exceptionally(e -> {
                LOGGER.error("Failed to initialise accept state handler: {}", e.getMessage(), e);
                queueService.resetPlayerQueue();
                return null;
            });

        }
    }

    private void handleUnkeen(SlashCommandInteractionEvent slashCommandInteractionEvent) {
        boolean activeQueuePop = queueService.isPlayerQueueFull();
        QueueInteractionOutcome queueInteractionOutcome = queueService.leavePlayerQueue(slashCommandInteractionEvent);

        switch (queueInteractionOutcome) {
            case ALREADY_NOT_IN_QUEUE -> notifyService.sendReply(
                            slashCommandInteractionEvent, ALREADY_NOT_IN_QUEUE.getDescription(), true);
            case REMOVED_FROM_QUEUE -> handleSpanner(slashCommandInteractionEvent,
                    String.format("%s%s\nCurrent queue: [%d/%d]\n%s",
                            slashCommandInteractionEvent.getUser().getAsMention(),
                            REMOVED_FROM_QUEUE.getDescription(),
                            queueService.showQueue().size(),
                            MAX_QUEUE_SIZE,
                            queueService.showQueue().stream()
                                    .map(User::getAsMention)
                                    .collect(Collectors.joining(" ")))
                    );
        }

        if (queueInteractionOutcome.equals(REMOVED_FROM_QUEUE) && activeQueuePop) {
            // Cancel accept service's timeoutTask here, and set messageId after message is edited
            notifyService.editPoppedQueueMessage(
                    slashCommandInteractionEvent.getMessageChannel(),
                    acceptService.getActiveQueueMessageId(),
                    String.format("%s has spannered. The remaining keeners will be returned to the queue.",
                            slashCommandInteractionEvent.getUser().getAsMention()));
            acceptService.cancelActiveQueue(); // Consider adding messageId setter here and move cancelTask to above
            spannerRepository.incrementSpannerCount(slashCommandInteractionEvent.getUser().getIdLong());
        }
    }

    private void handleSpanner(SlashCommandInteractionEvent slashCommandInteractionEvent, String message) {
        notifyService.sendSilentReply(slashCommandInteractionEvent, message);
        spannerRepository.incrementSpannerCount(slashCommandInteractionEvent.getUser().getIdLong());
    }

    private void handleKeeners(SlashCommandInteractionEvent slashCommandInteractionEvent) {
        queueService.showQueue();
        notifyService.sendSilentReply(
                slashCommandInteractionEvent,
                String.format("Current keeners: %s [%d/%d]",
                        queueService.showQueue().stream()
                                .map(User::getAsMention)
                                .collect(Collectors.joining(", ")),
                        queueService.showQueue().size(),
                        MAX_QUEUE_SIZE)
        );
    }

    private void handleInvalidCommand() {
        throw new RuntimeException("Unknown command received.");
    }

}
