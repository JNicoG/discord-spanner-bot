package io.github.jnicog.discord.spanner.bot.event.tenman;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

public class TenManNoPollActiveEvent extends AbstractCommandResult<SlashCommandContext> {

    public TenManNoPollActiveEvent(SlashCommandContext context) {
        super(context);
    }
}
