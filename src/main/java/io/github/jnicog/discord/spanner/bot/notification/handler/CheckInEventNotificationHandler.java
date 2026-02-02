package io.github.jnicog.discord.spanner.bot.notification.handler;

import io.github.jnicog.discord.spanner.bot.event.CheckInEvent;

public class CheckInEventNotificationHandler implements CommandEventNotificationHandler<CheckInEvent> {

    @Override
    public Class<CheckInEvent> handledEventType() {
        return CheckInEvent.class;
    }

    @Override
    public void handle(CheckInEvent event) {

    }

}
