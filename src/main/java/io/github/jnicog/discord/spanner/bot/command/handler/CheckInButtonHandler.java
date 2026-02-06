package io.github.jnicog.discord.spanner.bot.command.handler;

import io.github.jnicog.discord.spanner.bot.checkin.CheckInService;
import io.github.jnicog.discord.spanner.bot.command.ButtonContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CheckInButtonHandler implements ButtonCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckInButtonHandler.class);

    private final CheckInService checkInService;

    public CheckInButtonHandler(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    @Override
    public String getCommandName() {
        return "checkInAccept";
    }

    @Override
    public boolean isEphemeral() {
        return false;
    }

    @Override
    public AbstractCommandResult<?> handleCommand(ButtonContext context) {

        return null;
    }

    @Override
    public long getMessageId() {
        return 0;
    }
}
