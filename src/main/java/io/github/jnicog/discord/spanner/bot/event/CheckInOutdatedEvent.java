package io.github.jnicog.discord.spanner.bot.event;

import io.github.jnicog.discord.spanner.bot.model.ChannelQueue;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class CheckInOutdatedEvent extends QueueEvent {
    private final ButtonInteractionEvent buttonInteractionEvent;

    public CheckInOutdatedEvent(ChannelQueue queue, ButtonInteractionEvent buttonInteractionEvent) {
        super(queue);
        this.buttonInteractionEvent = buttonInteractionEvent;
    }

    public ButtonInteractionEvent getButtonInteractionEvent() {
        return buttonInteractionEvent;
    }
}
