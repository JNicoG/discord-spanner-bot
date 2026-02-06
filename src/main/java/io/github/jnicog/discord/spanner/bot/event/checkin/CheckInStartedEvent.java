package io.github.jnicog.discord.spanner.bot.event.checkin;

import io.github.jnicog.discord.spanner.bot.checkin.CheckInSession;

public class CheckInStartedEvent implements CheckInEvent {

    private final CheckInSession session;

    public CheckInStartedEvent(CheckInSession session) {
        this.session = session;
    }

    public CheckInSession getSession() {
        return session;
    }

    @Override
    public long getCheckInMessageId() {
        return session.getMessageId();
    }
}
