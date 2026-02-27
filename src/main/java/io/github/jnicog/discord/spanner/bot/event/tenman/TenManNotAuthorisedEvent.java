package io.github.jnicog.discord.spanner.bot.event.tenman;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

public class TenManNotAuthorisedEvent extends AbstractCommandResult<SlashCommandContext> {

    public TenManNotAuthorisedEvent(SlashCommandContext context) {
        super(context);
    }
}
