package io.github.jnicog.discord.spanner.bot.service;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.util.Deque;

public interface NotifyService {

    void notifyQueueReady(Deque<User> queue, MessageChannel channel);
}
