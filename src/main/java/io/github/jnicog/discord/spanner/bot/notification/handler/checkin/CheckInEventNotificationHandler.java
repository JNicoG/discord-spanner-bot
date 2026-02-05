package io.github.jnicog.discord.spanner.bot.notification.handler.checkin;

import io.github.jnicog.discord.spanner.bot.event.CheckInEvent;
import io.github.jnicog.discord.spanner.bot.notification.handler.CommandEventNotificationHandler;
import org.springframework.stereotype.Component;

@Component
public class CheckInEventNotificationHandler implements CommandEventNotificationHandler<CheckInEvent> {

//    @Override
//    public Class<CheckInEvent> handledEventType() {
//        return CheckInEvent.class;
//    }

    @Override
    public void handle(CheckInEvent event) {

    }

}
