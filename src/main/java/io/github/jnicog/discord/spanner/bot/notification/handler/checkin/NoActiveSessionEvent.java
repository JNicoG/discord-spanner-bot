package io.github.jnicog.discord.spanner.bot.notification.handler.checkin;

import io.github.jnicog.discord.spanner.bot.event.checkin.CheckInEvent;

public class NoActiveSessionEvent implements CheckInEvent {
    @Override
    public long getCheckInMessageId() {
        return 0;
    }
}
