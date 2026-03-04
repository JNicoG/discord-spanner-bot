package io.github.jnicog.discord.spanner.bot.event.tenman;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

public class TenManInvalidDateRangeEvent extends AbstractCommandResult<SlashCommandContext> {

    private final String errorMessage;

    public TenManInvalidDateRangeEvent(SlashCommandContext context, String errorMessage) {
        super(context);
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() { return errorMessage; }
}
