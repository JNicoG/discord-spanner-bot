package io.github.jnicog.discord.spanner.bot.command.handler;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManNoPollActiveEvent;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManPollCancelledEvent;
import io.github.jnicog.discord.spanner.bot.tenman.TenManService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TenManCancelCommandHandler implements SlashCommandHandler {

    private final TenManService tenManService;

    public TenManCancelCommandHandler(TenManService tenManService) {
        this.tenManService = tenManService;
    }

    @Override
    public String getCommandName() {
        return "ten-man-cancel";
    }

    @Override
    public AbstractCommandResult<?> handleCommand(SlashCommandContext context) {
        Optional<Long> messageId = tenManService.cancelPoll(context.channelId());
        if (messageId.isPresent()) {
            return new TenManPollCancelledEvent(context, context.channelId(), messageId.get());
        }
        return new TenManNoPollActiveEvent(context);
    }
}
