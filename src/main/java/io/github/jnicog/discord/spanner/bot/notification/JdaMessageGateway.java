package io.github.jnicog.discord.spanner.bot.notification;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * JDA implementation of the MessageGateway interface.
 *
 * <p>This service is injected by event listeners (not ListenerAdapters), which are
 * instantiated after the JDA bean is fully created. The dependency chain is:</p>
 * <ol>
 *   <li>JDA bean is created with all ListenerAdapter beans</li>
 *   <li>Event listeners (e.g., LeaderboardSessionTimeoutListener) are created</li>
 *   <li>Event listeners inject MessageGateway, which injects JDA</li>
 * </ol>
 */
@Service
public class JdaMessageGateway implements MessageGateway {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdaMessageGateway.class);

    private final JDA jda;

    public JdaMessageGateway(JDA jda) {
        this.jda = jda;
    }

    @Override
    public CompletableFuture<Void> clearReactions(long channelId, long messageId) {
        MessageChannel channel = jda.getChannelById(MessageChannel.class, channelId);
        if (channel == null) {
            LOGGER.warn("Channel {} not found when attempting to clear reactions from message {}",
                    channelId, messageId);
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Channel with ID " + channelId + " not found")
            );
        }

        return channel.retrieveMessageById(messageId)
                .submit()
                .thenCompose(message -> message.clearReactions().submit())
                .whenComplete((result, error) -> {
                    if (error != null) {
                        LOGGER.warn("Failed to clear reactions from message {}: {}",
                                messageId, error.getMessage());
                    } else {
                        LOGGER.debug("Cleared reactions from message {}", messageId);
                    }
                });
    }

    @Override
    public CompletableFuture<Long> sendMessage(long channelId, String message) {
        MessageChannel channel = jda.getChannelById(MessageChannel.class, channelId);
        if (channel == null) {
            LOGGER.warn("Channel {} not found when attempting to send message", channelId);
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Channel with ID " + channelId + " not found")
            );
        }

        return channel.sendMessage(message)
                .submit()
                .thenApply(msg -> msg.getIdLong());
    }

    @Override
    public CompletableFuture<Void> editMessage(long channelId, long messageId, String newContent) {
        MessageChannel channel = jda.getChannelById(MessageChannel.class, channelId);
        if (channel == null) {
            LOGGER.warn("Channel {} not found when attempting to edit message {}",
                    channelId, messageId);
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Channel with ID " + channelId + " not found")
            );
        }

        return channel.editMessageById(messageId, newContent)
                .submit()
                .thenAccept(ignored -> LOGGER.debug("Edited message {}", messageId));
    }

    @Override
    public CompletableFuture<Void> editMessageEmbed(long channelId, long messageId, MessageEmbed embed) {
        MessageChannel channel = jda.getChannelById(MessageChannel.class, channelId);
        if (channel == null) {
            LOGGER.warn("Channel {} not found when attempting to edit message embed {}",
                    channelId, messageId);
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Channel with ID " + channelId + " not found")
            );
        }

        return channel.editMessageEmbedsById(messageId, embed)
                .submit()
                .thenAccept(ignored -> LOGGER.debug("Edited message embed {}", messageId));
    }
}


