package io.github.jnicog.discord.spanner.bot.notification;

import io.github.jnicog.discord.spanner.bot.checkin.CheckInSession;

import java.util.concurrent.CompletableFuture;

public interface CheckInMessageGateway {
    CompletableFuture<String> sendCheckInStartMessage(long channelId, String message);
    CompletableFuture<Void> attachCheckInButtons(long channelId, long messageId);
    void updateCheckInMessage(long channelId, String message);
}
