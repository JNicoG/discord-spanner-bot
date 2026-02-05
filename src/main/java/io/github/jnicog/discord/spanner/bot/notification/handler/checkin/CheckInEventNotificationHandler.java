package io.github.jnicog.discord.spanner.bot.notification.handler.checkin;

import io.github.jnicog.discord.spanner.bot.event.checkin.PlayerCheckInEvent;
import io.github.jnicog.discord.spanner.bot.notification.handler.CommandEventNotificationHandler;
import org.springframework.stereotype.Component;

@Component
public class CheckInEventNotificationHandler implements CommandEventNotificationHandler<PlayerCheckInEvent> {

    @Override
    public void handle(PlayerCheckInEvent event) throws Exception {

    }
}
