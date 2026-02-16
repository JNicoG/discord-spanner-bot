package io.github.jnicog.discord.spanner.bot.event.queue;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

/**
 * Event for when the queue is already full.
 */
public class QueueAlreadyFullEvent extends AbstractCommandResult<SlashCommandContext> {

    public QueueAlreadyFullEvent(SlashCommandContext context) {
        super(context);
    }
}

