package io.github.jnicog.discord.spanner.bot.event;

import io.github.jnicog.discord.spanner.bot.model.ChannelQueue;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

public class CheckInCompletedEvent extends QueueEvent {
    private final MessageChannel channel;
    private final User user; // The last user that checked-in which resulted in check-in completion

    public CheckInCompletedEvent(ChannelQueue queue, MessageChannel channel, User user) {
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
