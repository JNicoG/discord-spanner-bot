package io.github.jnicog.discord.spanner.bot.event;

import io.github.jnicog.discord.spanner.bot.model.ChannelQueue;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

public class CheckInAcceptEvent extends QueueEvent {
    private final User user;
    private final MessageChannel channel;

    public CheckInAcceptEvent(ChannelQueue queue, User user, MessageChannel channel) {
        super(queue);
        this.user = user;
        this.channel = channel;
    }

    public User getUser() {
        return user;
    }

    public MessageChannel getChannel() {
        return channel;
    }
}
