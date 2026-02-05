package io.github.jnicog.discord.spanner.bot.event;

import io.github.jnicog.discord.spanner.bot.command.CommandContext;

public abstract class AbstractCommandResult {

    public CommandContext context;

    protected AbstractCommandResult(CommandContext commandContext) {
        this.context = commandContext;
    }

    public CommandContext getContext() {
        return context;
    }
}
