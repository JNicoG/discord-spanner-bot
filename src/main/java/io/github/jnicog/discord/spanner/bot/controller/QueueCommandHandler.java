package io.github.jnicog.discord.spanner.bot.controller;

import com.google.common.base.Strings;
import io.github.jnicog.discord.spanner.bot.repository.SpannerRepository;
import io.github.jnicog.discord.spanner.bot.service.NotifyService;
import io.github.jnicog.discord.spanner.bot.service.QueueInteractionOutcome;
import io.github.jnicog.discord.spanner.bot.service.QueueService;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.stream.Collectors;

import static io.github.jnicog.discord.spanner.bot.service.QueueInteractionOutcome.ADDED_TO_QUEUE;
import static io.github.jnicog.discord.spanner.bot.service.QueueInteractionOutcome.ALREADY_IN_QUEUE;
import static io.github.jnicog.discord.spanner.bot.service.QueueInteractionOutcome.ALREADY_NOT_IN_QUEUE;
import static io.github.jnicog.discord.spanner.bot.service.QueueInteractionOutcome.QUEUE_ALREADY_FULL;
import static io.github.jnicog.discord.spanner.bot.service.QueueInteractionOutcome.REMOVED_FROM_QUEUE;
import static io.github.jnicog.discord.spanner.bot.service.QueueServiceImpl.MAX_QUEUE_SIZE;

@Component
public class QueueCommandHandler extends ListenerAdapter {

    private final QueueService queueService;
    private final NotifyService notifyService;
    private final SpannerRepository spannerRepository;

    @Autowired
    public QueueCommandHandler(QueueService queueService,
                               NotifyService notifyService,
                               SpannerRepository spannerRepository) {
        this.queueService = queueService;
        this.notifyService = notifyService;
        this.spannerRepository = spannerRepository;
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
        /*queueService.updateAcceptList(buttonInteractionEvent.getUser());*/
    }

    private void handleSpannerButton(ButtonInteractionEvent buttonInteractionEvent) {
        queueService.removeUserFromPlayerQueue(buttonInteractionEvent.getUser());
        spannerRepository.incrementSpannerCount(buttonInteractionEvent.getUser().getIdLong());
        notifyService.notifyPoppedQueueDeclined(buttonInteractionEvent,
                String.format("%s has spannered. The remaining keeners will be returned to the queue.",
                buttonInteractionEvent.getUser().getAsMention()));
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
            case ADDED_TO_QUEUE -> notifyService.sendReply(
                    slashCommandInteractionEvent,
                    String.format("%s%s [%d/%d]",
                            slashCommandInteractionEvent.getUser().getAsMention(),
                            ADDED_TO_QUEUE.getDescription(),
                            queueService.showQueue().size(),
                            MAX_QUEUE_SIZE),
                    false);
        }

        if (queueInteractionOutcome.equals(ADDED_TO_QUEUE) && queueService.getQueuePoppedState()) {
            notifyService.notifyPlayerQueuePopped(
                    queueService.getPlayerQueue(), slashCommandInteractionEvent.getMessageChannel());
        }
    }

    private void handleUnkeen(SlashCommandInteractionEvent slashCommandInteractionEvent) {
        QueueInteractionOutcome queueInteractionOutcome = queueService.leavePlayerQueue(slashCommandInteractionEvent);
        boolean activeQueuePop = queueService.getQueuePoppedState();

        switch (queueInteractionOutcome) {
            case ALREADY_NOT_IN_QUEUE -> notifyService.sendReply(
                            slashCommandInteractionEvent, ALREADY_NOT_IN_QUEUE.getDescription(), true);
            case REMOVED_FROM_QUEUE -> handleSpanner(slashCommandInteractionEvent,
                    String.format("%s%s [%d/%d]",
                            slashCommandInteractionEvent.getUser().getAsMention(),
                            REMOVED_FROM_QUEUE.getDescription(),
                            queueService.showQueue().size(),
                            MAX_QUEUE_SIZE));
        }

        if (queueInteractionOutcome.equals(REMOVED_FROM_QUEUE) && activeQueuePop) {
            notifyService.notifyPoppedQueueDeclined(
                    slashCommandInteractionEvent/*.getMessageChannel()*/,
                    String.format("%s declined and has received a spanner." +
                                    " The remaining players will be returned to the queue.",
                            slashCommandInteractionEvent.getUser().getAsMention()));
            spannerRepository.incrementSpannerCount(slashCommandInteractionEvent.getUser().getIdLong());
        }
    }

    private void handleSpanner(SlashCommandInteractionEvent slashCommandInteractionEvent, String message) {
        notifyService.sendReply(slashCommandInteractionEvent, message, false);
        spannerRepository.incrementSpannerCount(slashCommandInteractionEvent.getUser().getIdLong());
    }

    private void handleKeeners(SlashCommandInteractionEvent slashCommandInteractionEvent) {
        queueService.showQueue();
        notifyService.sendSilentReply(
                slashCommandInteractionEvent,
                String.format("[%d/%d] Current keeners: %s",
                        queueService.showQueue().size(),
                        MAX_QUEUE_SIZE,
                        queueService.showQueue().stream()
                                .map(User::getAsMention)
                                .collect(Collectors.joining(", "))));
    }

    private void handleInvalidCommand() {
        throw new RuntimeException("Unknown command received.");
    }

}
