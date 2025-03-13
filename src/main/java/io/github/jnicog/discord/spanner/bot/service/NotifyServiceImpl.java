package io.github.jnicog.discord.spanner.bot.service;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class NotifyServiceImpl implements NotifyService {

    private Message queuePopMessage;

    private final Map<Long, Boolean> userAcceptanceStatus = new ConcurrentHashMap<>();

    @Autowired
    public NotifyServiceImpl() {
        // Empty constructor
    }

    @Override
    public void notifyPlayerQueuePopped(Map<User, KeenMetadata> queue, MessageChannel messageChannel) {
        messageChannel.sendMessage(String.format("The queue has been filled!" +
                        " Click on the %s button within 3 minutes to accept." +
                        " %s",
                Emoji.fromUnicode("U+2705"),
                queue.keySet()
                        .stream()
                        .map(User::getAsMention)
                        .collect(Collectors.joining(" "))))
                .queue();

        for (User user : queue.keySet()) {
            queue.get(user).getKeenInteractionEvent().reply("a")
                    .setEphemeral(true)
                    .setActionRow(Button.success("acceptButton", Emoji.fromUnicode("U+2705")))
                    .setActionRow(Button.danger("spannerButton", Emoji.fromUnicode("U+1F527")))
                    .queue();
        }
    }

    @Override
    public void notifyPoppedQueueAccepted(Set<User> queue, MessageChannel messageChannel) {

    }

    @Override
    public void notifyPoppedQueueDeclined(GenericEvent interactionEvent, String message) {
        if (interactionEvent instanceof ButtonInteractionEvent) {
            ((ButtonInteractionEvent) interactionEvent)
                    .deferEdit().queue();

            ((ButtonInteractionEvent) interactionEvent)
                    .getMessage()
                    .editMessage(message)
                    .setAllowedMentions(Collections.emptyList())
                    .setComponents(Collections.emptyList())
                    .queue();
        }
        if (interactionEvent instanceof SlashCommandInteractionEvent) {
            ((SlashCommandInteractionEvent) interactionEvent).getMessageChannel()
                    .sendMessage(message)
                    .setAllowedMentions(Collections.emptyList())
                    .queue();
        }
    }

    @Override
    public void notifyPoppedQueueTimeout(Set<User> queue, MessageChannel messageChannel) {

    }

    @Override
    public void notifyPoppedQueuePlayerAccept(ButtonInteractionEvent buttonInteractionEvent) {

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
