package io.github.jnicog.discord.spanner.bot.command.handler;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManResignErrorEvent;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManRosterResignedSlashEvent;
import io.github.jnicog.discord.spanner.bot.tenman.TenManCooldownService;
import io.github.jnicog.discord.spanner.bot.tenman.TenManResignSlashResult;
import io.github.jnicog.discord.spanner.bot.tenman.TenManService;
import org.springframework.stereotype.Component;

@Component
public class TenManResignCommandHandler implements SlashCommandHandler {

    private final TenManService tenManService;
    private final TenManCooldownService cooldownService;

    public TenManResignCommandHandler(TenManService tenManService, TenManCooldownService cooldownService) {
        this.tenManService = tenManService;
        this.cooldownService = cooldownService;
    }

    @Override
    public String getCommandName() {
        return "ten-man-resign";
    }

    @Override
    public AbstractCommandResult<?> handleCommand(SlashCommandContext context) {
        java.util.Optional<Long> lockedIdOpt = tenManService.getLockedDateOptionId(context.channelId());
        if (lockedIdOpt.isPresent() && cooldownService.isOnCooldown(context.userId(), lockedIdOpt.get())) {
            long secs = cooldownService.remainingCooldown(context.userId(), lockedIdOpt.get()).getSeconds();
            long mins = secs / 60;
            long rem = secs % 60;
            String timeLeft = mins > 0 ? mins + "m " + rem + "s" : secs + "s";
            return new TenManResignErrorEvent(context, "Easy there! You recently changed your sign-up. Wait " + timeLeft + " before changing it again.");
        }

        TenManResignSlashResult result = tenManService.resignFromLockedRoster(context.channelId(), context.userId());

        return switch (result) {
            case TenManResignSlashResult.NoPoll np ->
                    new TenManResignErrorEvent(context, "There is no active ten-man roster in this channel.");
            case TenManResignSlashResult.NotOnRoster nor ->
                    new TenManResignErrorEvent(context, "You are not on the ten-man roster.");
            case TenManResignSlashResult.Resigned r -> {
                cooldownService.recordAction(context.userId(), r.dateOptionId());
                yield new TenManRosterResignedSlashEvent(context, context.userId(), context.channelId(),
                        r.dateOptionId(), r.date(), r.timeDisplay(), r.slotsNeeded());
            }
        };
    }
}
