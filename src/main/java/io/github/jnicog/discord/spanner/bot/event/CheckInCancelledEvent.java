package io.github.jnicog.discord.spanner.bot.event;

import io.github.jnicog.discord.spanner.bot.model.ChannelQueue;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

public class CheckInCancelledEvent extends QueueEvent {
    private final MessageChannel channel;
    private final User user;

    public CheckInCancelledEvent(ChannelQueue queue, MessageChannel channel, User user) {
        super(queue);
        this.channel = channel;
        this.user = user;
    }

    public MessageChannel getChannel() {
        return channel;
    }

    public User getUser() {
        return user;
    }
}
