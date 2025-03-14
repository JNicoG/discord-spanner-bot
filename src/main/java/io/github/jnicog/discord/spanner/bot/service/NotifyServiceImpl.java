package io.github.jnicog.discord.spanner.bot.service;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static io.github.jnicog.discord.spanner.bot.service.AcceptServiceImpl.ACCEPT_TIMEOUT_LENGTH;
import static io.github.jnicog.discord.spanner.bot.service.AcceptServiceImpl.TIME_UNIT;
import static io.github.jnicog.discord.spanner.bot.service.Constants.acceptButton;
import static io.github.jnicog.discord.spanner.bot.service.Constants.awaitingButton;
import static io.github.jnicog.discord.spanner.bot.service.Constants.checkMarkEmoji;
import static io.github.jnicog.discord.spanner.bot.service.Constants.spannerButton;

@Service
public class NotifyServiceImpl implements NotifyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotifyServiceImpl.class);

    @Autowired
    public NotifyServiceImpl() {
        // Empty constructor
    }

    @Override
    public CompletableFuture<Long> notifyPlayerQueuePopped(Map<User, Long> queue, MessageChannel messageChannel) {
        CompletableFuture<Long> futureMessageId = new CompletableFuture<>();

        MessageCreateAction messageCreateAction = messageChannel.sendMessage(
                        String.format("The queue has been filled!" +
                                        "\nClick the %s button within %d %s to accept." +
                                        "\nWaiting for all players to accept..." +
                                        "\n%s",
                                checkMarkEmoji,
                                ACCEPT_TIMEOUT_LENGTH,
                                TIME_UNIT.toString().toLowerCase(),
                                queue.keySet()
                                        .stream()
                                        .map(user -> String.format("%s [%s]", user.getAsMention(), awaitingButton))
                                        .collect(Collectors.joining(" | "))))
                .addActionRow(acceptButton, spannerButton);
        messageCreateAction.queue(message -> {
            long messageId = message.getIdLong();
            futureMessageId.complete(messageId);
            LOGGER.info("Sent queue pop message - ID: {}", messageId);
        }, error -> {
            LOGGER.error("Failed to send queue pop message: {}", error.getMessage());
            futureMessageId.completeExceptionally(error);
        });

        return futureMessageId;
    }

    @Override
    public void notifyPoppedQueueAccepted(ButtonInteractionEvent buttonInteractionEvent, String message) {
        buttonInteractionEvent.deferEdit().queue();
        buttonInteractionEvent.getMessage().editMessage(message)
                .setComponents(Collections.emptyList())
                .queue();
    }

    @Override
    public void editPoppedQueueMessage(MessageChannel messageChannel, long activeQueueMessageId, String message) {
        messageChannel.editMessageById(activeQueueMessageId, message).setComponents(Collections.emptyList()).queue(
                success -> LOGGER.info("Timeout message edited successfully"),
                error -> LOGGER.error("Failed to edit message by timeout: {}", error.getMessage(), error)
        );
    }

    @Override
    public void notifyPoppedQueuePlayerAccept(ButtonInteractionEvent buttonInteractionEvent,
                                              String message) {
        buttonInteractionEvent.getMessage().editMessage(message).queue();

    }

    @Override
    public void sendReply(IReplyCallback genericEvent, String message, boolean isEphemeral) {
        genericEvent.reply(message)
                .setEphemeral(isEphemeral)
                .queue();
    }

    @Override
    public void sendSilentReply(IReplyCallback genericEvent, String message) {
        genericEvent.reply(message)
                .setEphemeral(false)
                .setAllowedMentions(Collections.emptyList())
                .queue();
    }

}
