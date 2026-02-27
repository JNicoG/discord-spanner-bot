package io.github.jnicog.discord.spanner.bot.command.handler;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManNoPollActiveEvent;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManNotAuthorisedEvent;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManPollCancelledEvent;
import io.github.jnicog.discord.spanner.bot.tenman.TenManPermissionChecker;
import io.github.jnicog.discord.spanner.bot.tenman.TenManService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TenManCancelCommandHandler implements SlashCommandHandler {

    private final TenManService tenManService;
    private final TenManPermissionChecker permissionChecker;

    public TenManCancelCommandHandler(TenManService tenManService, TenManPermissionChecker permissionChecker) {
        this.tenManService = tenManService;
        this.permissionChecker = permissionChecker;
    }

    @Override
    public String getCommandName() {
        return "ten-man-cancel";
    }

    @Override
    public AbstractCommandResult<?> handleCommand(SlashCommandContext context) {
        if (!permissionChecker.isAllowed(context.username())) {
            return new TenManNotAuthorisedEvent(context);
        }

        Optional<Long> messageId = tenManService.cancelPoll(context.channelId());
        if (messageId.isPresent()) {
            return new TenManPollCancelledEvent(context, context.channelId(), messageId.get());
        }
        return new TenManNoPollActiveEvent(context);
    }
}
