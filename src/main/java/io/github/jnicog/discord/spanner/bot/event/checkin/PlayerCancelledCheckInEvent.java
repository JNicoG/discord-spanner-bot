package io.github.jnicog.discord.spanner.bot.event.checkin;

import io.github.jnicog.discord.spanner.bot.checkin.CheckInSession;
import io.github.jnicog.discord.spanner.bot.command.InteractionContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import io.github.jnicog.discord.spanner.bot.event.spanner.SpannerAwardingEvent;

public class PlayerCancelledCheckInEvent extends AbstractCommandResult<InteractionContext> implements SpannerAwardingEvent, CheckInEvent {

    private final CheckInSession activeSession;

    public PlayerCancelledCheckInEvent(InteractionContext commandContext,
                                       CheckInSession activeSession) {
        super(commandContext);
        this.activeSession = activeSession;
    }

    public CheckInSession getActiveSession() {
        return activeSession;
    }

    @Override
    public long getTargetUserId() {
        return this.getContext().userId();
    }

    @Override
    public long getTargetChannelId() {
        return this.getContext().channelId();
    }

    @Override
    public long getCheckInMessageId() {
        return activeSession.getMessageId();
    }
}
