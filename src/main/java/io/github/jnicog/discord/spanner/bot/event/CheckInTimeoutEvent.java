package io.github.jnicog.discord.spanner.bot.event;

import io.github.jnicog.discord.spanner.bot.model.ChannelQueue;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.util.Set;

public class CheckInTimeoutEvent extends QueueEvent {
    private final MessageChannel channel;
    private final Set<User> notCheckedInUsers;

    public CheckInTimeoutEvent(ChannelQueue queue, MessageChannel channel, Set<User> notCheckedInUsers) {
        super(queue);
        this.channel = channel;
        this.notCheckedInUsers = notCheckedInUsers;
    }

    public MessageChannel getChannel() {
        return channel;
    }

    public Set<User> getNotCheckedInUsers() {
        return notCheckedInUsers;
    }
}
