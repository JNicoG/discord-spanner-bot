package io.github.jnicog.discord.spanner.bot.notification;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import static io.github.jnicog.discord.spanner.bot.notification.handler.checkin.CheckInStartedEventNotificationHandler.ACCEPT_BUTTON;
import static io.github.jnicog.discord.spanner.bot.notification.handler.checkin.CheckInStartedEventNotificationHandler.SPANNER_BUTTON;

@Service
public class JdaCheckInMessageGatewayImpl implements CheckInMessageGateway {

    private final JDA jda;



    public JdaCheckInMessageGatewayImpl(JDA jda) {
        this.jda = jda;
    }

    @Override
    public CompletableFuture<String> sendCheckInStartMessage(long channelId, String message) {
        TextChannel channel = jda.getTextChannelById(channelId);
        if (channel == null) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Channel with ID " + channelId + " not found")
            );
        }

        return channel.sendMessage(message)
                .submit()
                .thenApply(Message::getId);
    }

    @Override
    public CompletableFuture<Void> attachCheckInButtons(long channelId, long messageId) {
        TextChannel channel = jda.getTextChannelById(channelId);
        if (channel == null) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Channel with ID " + channelId + " not found")
            );
        }

        return channel.editMessageComponentsById(messageId, ActionRow.of(ACCEPT_BUTTON, SPANNER_BUTTON))
                .submit()
                .thenAccept(ignored -> {});
    }

    @Override
    public void updateCheckInMessage(long channelId, String message) {

    }
}
