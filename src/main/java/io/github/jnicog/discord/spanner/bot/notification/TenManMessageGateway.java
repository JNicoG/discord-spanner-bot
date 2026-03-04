package io.github.jnicog.discord.spanner.bot.notification;

import net.dv8tion.jda.api.components.buttons.Button;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface TenManMessageGateway {

    CompletableFuture<Long> sendPollMessage(long channelId, String formattedContent, List<Button> dateButtons);

    void updatePollMessage(long channelId, long messageId, String formattedContent);

    void sendNotification(long channelId, String content);

    void deleteMessage(long channelId, long messageId);
}
