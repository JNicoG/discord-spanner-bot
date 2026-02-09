package io.github.jnicog.discord.spanner.bot.notification;

import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.concurrent.CompletableFuture;

/**
 * Gateway for performing message operations in Discord channels.
 *
 * <p>This interface abstracts JDA-specific message operations, allowing
 * components that are not part of the JDA lifecycle (like event listeners)
 * to perform message operations without directly depending on JDA.</p>
 */
public interface MessageGateway {

    /**
     * Clears all reactions from a message.
     *
     * @param channelId The channel ID containing the message
     * @param messageId The message ID to clear reactions from
     * @return CompletableFuture that completes when reactions are cleared
     */
    CompletableFuture<Void> clearReactions(long channelId, long messageId);

    /**
     * Sends a new message to a channel.
     *
     * @param channelId The channel ID to send the message to
     * @param message The message content
     * @return CompletableFuture containing the sent message ID
     */
    CompletableFuture<Long> sendMessage(long channelId, String message);

    /**
     * Edits an existing message.
     *
     * @param channelId The channel ID containing the message
     * @param messageId The message ID to edit
     * @param newContent The new message content
     * @return CompletableFuture that completes when the edit is done
     */
    CompletableFuture<Void> editMessage(long channelId, long messageId, String newContent);

    /**
     * Edits an existing message to display an embed.
     *
     * @param channelId The channel ID containing the message
     * @param messageId The message ID to edit
     * @param embed The embed to display
     * @return CompletableFuture that completes when the edit is done
     */
    CompletableFuture<Void> editMessageEmbed(long channelId, long messageId, MessageEmbed embed);
}

