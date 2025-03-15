package io.github.jnicog.discord.spanner.bot.service;

import io.github.jnicog.discord.spanner.bot.model.ChannelQueue;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

public interface ChannelQueueManager {

    ChannelQueue getOrCreateQueue(MessageChannel messageChannel);

}
