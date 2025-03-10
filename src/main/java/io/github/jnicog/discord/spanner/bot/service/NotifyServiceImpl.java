package io.github.jnicog.discord.spanner.bot.service;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Deque;

@Service
public class NotifyServiceImpl implements NotifyService {

    @Autowired
    public NotifyServiceImpl() {
    }

    @Override
    public void notifyQueueReady(Deque<User> queue, MessageChannel channel) {

    }
}
