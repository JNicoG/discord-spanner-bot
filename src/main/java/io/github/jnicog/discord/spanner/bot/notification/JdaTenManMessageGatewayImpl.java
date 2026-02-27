package io.github.jnicog.discord.spanner.bot.notification;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class JdaTenManMessageGatewayImpl implements TenManMessageGateway {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdaTenManMessageGatewayImpl.class);

    private final JDA jda;

    public JdaTenManMessageGatewayImpl(JDA jda) {
        this.jda = jda;
    }

    @Override
    public CompletableFuture<Long> sendPollMessage(long channelId, String formattedContent, List<Button> dateButtons) {
        TextChannel channel = jda.getTextChannelById(channelId);
        if (channel == null) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Channel with ID " + channelId + " not found")
            );
        }

        List<ActionRow> actionRows = new ArrayList<>();
        for (int i = 0; i < dateButtons.size(); i += 5) {
            actionRows.add(ActionRow.of(dateButtons.subList(i, Math.min(i + 5, dateButtons.size()))));
        }

        return channel.sendMessage(formattedContent)
                .setComponents(actionRows)
                .submit()
                .thenApply(message -> message.getIdLong());
    }

    @Override
    public void updatePollMessage(long channelId, long messageId, String formattedContent) {
        TextChannel channel = jda.getTextChannelById(channelId);
        if (channel == null) {
            LOGGER.error("Channel with ID {} not found when updating poll message", channelId);
            return;
        }

        channel.editMessageById(messageId, formattedContent)
                .queue(
                        __ -> {},
                        error -> LOGGER.error("Failed to update poll message {}: {}", messageId, error.getMessage())
                );
    }

    @Override
    public void deleteMessage(long channelId, long messageId) {
        TextChannel channel = jda.getTextChannelById(channelId);
        if (channel == null) {
            LOGGER.error("Channel with ID {} not found when deleting poll message", channelId);
            return;
        }

        channel.deleteMessageById(messageId)
                .queue(
                        __ -> {},
                        error -> LOGGER.error("Failed to delete poll message {}: {}", messageId, error.getMessage())
                );
    }

    @Override
    public void sendNotification(long channelId, String content) {
        TextChannel channel = jda.getTextChannelById(channelId);
        if (channel == null) {
            LOGGER.error("Channel with ID {} not found when sending notification", channelId);
            return;
        }

        channel.sendMessage(content)
                .queue(
                        __ -> {},
                        error -> LOGGER.error("Failed to send ten-man notification to channel {}: {}", channelId, error.getMessage())
                );
    }
}
