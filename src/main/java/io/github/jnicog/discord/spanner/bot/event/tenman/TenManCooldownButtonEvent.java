package io.github.jnicog.discord.spanner.bot.event.tenman;

import io.github.jnicog.discord.spanner.bot.command.ButtonInteractionContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

public class TenManCooldownButtonEvent extends AbstractCommandResult<ButtonInteractionContext> {

    private final long userId;
    private final long remainingSeconds;

    public TenManCooldownButtonEvent(ButtonInteractionContext context, long userId, long remainingSeconds) {
        super(context);
        this.userId = userId;
        this.remainingSeconds = remainingSeconds;
    }

    public long getUserId() { return userId; }
    public long getRemainingSeconds() { return remainingSeconds; }
}
