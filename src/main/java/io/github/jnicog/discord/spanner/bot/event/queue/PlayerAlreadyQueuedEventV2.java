package io.github.jnicog.discord.spanner.bot.event.queue;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResultV2;

/**
 * V2 event for when a player is already in the queue.
 */
public class PlayerAlreadyQueuedEventV2 extends AbstractCommandResultV2<SlashCommandContext> {

    public PlayerAlreadyQueuedEventV2(SlashCommandContext context) {
        super(context);
    }
}

