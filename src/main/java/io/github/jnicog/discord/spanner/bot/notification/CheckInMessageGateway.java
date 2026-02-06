package io.github.jnicog.discord.spanner.bot.notification;

import java.util.concurrent.CompletableFuture;

/**
 * Gateway for sending and managing check-in messages in Discord channels.
 *
 * <p>This interface handles different use cases:</p>
 *
 * <ul>
 *   <li><b>Proactive messaging</b> - {@link #sendCheckInStartMessage} sends a NEW message to a channel,
 *       not a response to a user interaction. The check-in start is triggered by an internal event.</li>
 *   <li><b>Button attachment</b> - {@link #attachCheckInButtons} adds interactive components to an existing message.</li>
 *   <li><b>Cross-interaction updates</b> - {@link #updateCheckInMessageAndClearButtons} updates a message
 *       that was NOT triggered by the current interaction (e.g., when /unkeen cancels a session,
 *       the check-in message is updated separately from the slash command response).</li>
 * </ul>
 *
 * <p>The {@code InteractionResponse} pattern handles <b>responses to user interactions</b> (slash commands,
 * button clicks), while this gateway handles <b>bot-initiated messages</b> and <b>message manipulation</b>
 * that occurs outside the request-response cycle.</p>
 */
public interface CheckInMessageGateway {
    CompletableFuture<String> sendCheckInStartMessage(long channelId, String message);
    CompletableFuture<Void> attachCheckInButtons(long channelId, long messageId);

    /**
     * Updates the check-in message content and removes all buttons/components.
     * @param channelId The channel ID
     * @param messageId The message ID to update
     * @param message The new message content
     * @return CompletableFuture that completes when the update is done
     */
    CompletableFuture<Void> updateCheckInMessageAndClearButtons(long channelId, long messageId, String message);
}
