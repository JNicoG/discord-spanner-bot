package io.github.jnicog.discord.spanner.bot.event.queue;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

/**
 * Event for when a player tries to leave but is not in the queue.
 */
public class PlayerNotInQueueEvent extends AbstractCommandResult<SlashCommandContext> {

    public PlayerNotInQueueEvent(SlashCommandContext context) {
        super(context);
    }
}

