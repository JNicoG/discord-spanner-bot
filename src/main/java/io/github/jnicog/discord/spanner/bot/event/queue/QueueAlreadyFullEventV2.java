package io.github.jnicog.discord.spanner.bot.event.queue;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResultV2;

/**
 * V2 event for when the queue is already full.
 */
public class QueueAlreadyFullEventV2 extends AbstractCommandResultV2<SlashCommandContext> {

    public QueueAlreadyFullEventV2(SlashCommandContext context) {
        super(context);
    }
}

