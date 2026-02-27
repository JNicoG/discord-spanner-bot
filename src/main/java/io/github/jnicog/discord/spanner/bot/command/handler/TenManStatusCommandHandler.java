package io.github.jnicog.discord.spanner.bot.command.handler;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManNoPollActiveEvent;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManStatusEvent;
import io.github.jnicog.discord.spanner.bot.tenman.TenManService;
import org.springframework.stereotype.Component;

@Component
public class TenManStatusCommandHandler implements SlashCommandHandler {

    private final TenManService tenManService;

    public TenManStatusCommandHandler(TenManService tenManService) {
        this.tenManService = tenManService;
    }

    @Override
    public String getCommandName() {
        return "ten-man-status";
    }

    @Override
    public AbstractCommandResult<?> handleCommand(SlashCommandContext context) {
        return tenManService.getActivePollSnapshot(context.channelId())
                .<AbstractCommandResult<?>>map(snapshot -> new TenManStatusEvent(context, snapshot))
                .orElse(new TenManNoPollActiveEvent(context));
    }
}
