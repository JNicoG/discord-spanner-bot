package io.github.jnicog.discord.spanner.bot.event.queue;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResultV2;

/**
 * V2 event for when a player tries to leave but is not in the queue.
 */
public class PlayerNotInQueueEventV2 extends AbstractCommandResultV2<SlashCommandContext> {

    public PlayerNotInQueueEventV2(SlashCommandContext context) {
        super(context);
    }
}

