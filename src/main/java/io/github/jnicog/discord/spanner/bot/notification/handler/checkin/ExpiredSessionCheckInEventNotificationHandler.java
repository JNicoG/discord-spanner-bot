package io.github.jnicog.discord.spanner.bot.notification.handler.checkin;

import io.github.jnicog.discord.spanner.bot.event.checkin.ExpiredSessionCheckInEvent;
import io.github.jnicog.discord.spanner.bot.notification.handler.CommandEventNotificationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ExpiredSessionCheckInEventNotificationHandler implements CommandEventNotificationHandler<ExpiredSessionCheckInEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpiredSessionCheckInEventNotificationHandler.class);

    @Override
    public void handle(ExpiredSessionCheckInEvent event) throws Exception {
        event.getContext().interactionResponder().replyEphemeral("This check-in session is no longer active.");
    }
}
