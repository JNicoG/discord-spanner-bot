package io.github.jnicog.discord.spanner.bot.controller;

import com.google.common.base.Strings;
import io.github.jnicog.discord.spanner.bot.service.QueueService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class QueueCommandHandler extends ListenerAdapter {

    private final QueueService queueService;

    @Autowired
    public QueueCommandHandler(QueueService queueService) {
        this.queueService = queueService;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent slashCommandInteractionEvent) {
        if (!Strings.isNullOrEmpty(slashCommandInteractionEvent.getName())) {
            routeSlashCommand(slashCommandInteractionEvent);
        } else {
            throw new RuntimeException("Invalid slash command.");
        }
    }

    private void routeSlashCommand(SlashCommandInteractionEvent slashCommandInteractionEvent) {
        switch (slashCommandInteractionEvent.getName()) {
            case "k":
            case "keen":
                handleKeen(slashCommandInteractionEvent);
                break;
            case "unkeen":
                handleUnkeen(slashCommandInteractionEvent);
                break;
            default:
                handleInvalidCommand();
        }
    }

    private void handleKeen(SlashCommandInteractionEvent slashCommandInteractionEvent) {
        queueService.joinPlayerQueue(slashCommandInteractionEvent);
    }

    private void handleUnkeen(SlashCommandInteractionEvent slashCommandInteractionEvent) {
        queueService.leavePlayerQueue(slashCommandInteractionEvent);
    }

    private void handleInvalidCommand() {
        throw new RuntimeException("Unknown command received.");
    }

}
