package io.github.jnicog.discord.spanner.bot.service;

import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public interface AcceptService {

    void playerAccept(ButtonInteractionEvent interactionEvent);

    void initialiseAcceptQueue(long queuePopMessageId, MessageChannel messageChannel);

    boolean isActiveQueueMessage(long messageId);

    long getActiveQueueMessageId();

    void cancelActiveQueue();

}
