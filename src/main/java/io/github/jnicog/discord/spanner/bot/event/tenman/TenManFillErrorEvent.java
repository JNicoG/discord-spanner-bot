package io.github.jnicog.discord.spanner.bot.event.tenman;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

public class TenManFillErrorEvent extends AbstractCommandResult<SlashCommandContext> {

    private final String message;

    public TenManFillErrorEvent(SlashCommandContext context, String message) {
        super(context);
        this.message = message;
    }

    public String getMessage() { return message; }
}
