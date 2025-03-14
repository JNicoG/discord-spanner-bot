package io.github.jnicog.discord.spanner.bot.service;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public interface AcceptService {

    void playerAccept(ButtonInteractionEvent interactionEvent);

    void initialiseQueue(long queuePopMessageId);

    boolean isActiveQueueMessage(long messageId);

}
