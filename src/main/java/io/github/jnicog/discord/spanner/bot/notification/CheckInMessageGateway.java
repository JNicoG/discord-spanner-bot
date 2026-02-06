package io.github.jnicog.discord.spanner.bot.notification;

import java.util.concurrent.CompletableFuture;

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
