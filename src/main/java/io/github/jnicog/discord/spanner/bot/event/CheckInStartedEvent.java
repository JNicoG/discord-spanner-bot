package io.github.jnicog.discord.spanner.bot.event;

import io.github.jnicog.discord.spanner.bot.model.ChannelQueue;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

public class CheckInStartedEvent extends QueueEvent {
    private final MessageChannel channel;

    public CheckInStartedEvent(ChannelQueue queue, MessageChannel channel) {
        super(queue);
        this.channel = channel;
    }

    public MessageChannel getChannel() {
        return channel;
    }
}
