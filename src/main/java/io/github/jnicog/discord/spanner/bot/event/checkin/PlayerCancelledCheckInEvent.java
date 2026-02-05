package io.github.jnicog.discord.spanner.bot.event.checkin;

import io.github.jnicog.discord.spanner.bot.command.CommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import io.github.jnicog.discord.spanner.bot.event.spanner.SpannerAwardingEvent;

public class PlayerCancelledCheckInEvent extends AbstractCommandResult implements SpannerAwardingEvent {

    protected PlayerCancelledCheckInEvent(CommandContext commandContext) {
        super(commandContext);
    }

    @Override
    public long getTargetUserId() {
        return context.userId();
    }

    @Override
    public long getTargetChannelId() {
        return context.channelId();
    }
}
