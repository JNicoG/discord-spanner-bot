package io.github.jnicog.discord.spanner.bot.notification;

import java.util.concurrent.CompletableFuture;

public interface CheckInMessageGateway {
    CompletableFuture<String> sendCheckInStartMessage(long channelId, String message);
    CompletableFuture<Void> attachCheckInButtons(long channelId, long messageId);
}
