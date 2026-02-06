package io.github.jnicog.discord.spanner.bot.event.queue;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

/**
 *  event for when a player is already in the queue.
 */
public class PlayerAlreadyQueuedEvent extends AbstractCommandResult<SlashCommandContext> {

    public PlayerAlreadyQueuedEvent(SlashCommandContext context) {
        super(context);
    }
}

