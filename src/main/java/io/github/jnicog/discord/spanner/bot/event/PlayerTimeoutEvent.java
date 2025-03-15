package io.github.jnicog.discord.spanner.bot.event;

import io.github.jnicog.discord.spanner.bot.model.ChannelQueue;
import net.dv8tion.jda.api.entities.User;

public class PlayerTimeoutEvent extends QueueEvent {
    private final User user;

    public PlayerTimeoutEvent(ChannelQueue queue, User user) {
        super(queue);
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
