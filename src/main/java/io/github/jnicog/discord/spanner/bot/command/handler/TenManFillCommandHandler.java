package io.github.jnicog.discord.spanner.bot.command.handler;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManFillErrorEvent;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManFillSuccessEvent;
import io.github.jnicog.discord.spanner.bot.tenman.TenManCooldownService;
import io.github.jnicog.discord.spanner.bot.tenman.TenManFillSlashResult;
import io.github.jnicog.discord.spanner.bot.tenman.TenManService;
import org.springframework.stereotype.Component;

@Component
public class TenManFillCommandHandler implements SlashCommandHandler {

    private final TenManService tenManService;
    private final TenManCooldownService cooldownService;

    public TenManFillCommandHandler(TenManService tenManService, TenManCooldownService cooldownService) {
        this.tenManService = tenManService;
        this.cooldownService = cooldownService;
    }

    @Override
    public String getCommandName() {
        return "ten-man-fill";
    }

    @Override
    public AbstractCommandResult<?> handleCommand(SlashCommandContext context) {
        java.util.Optional<Long> lockedIdOpt = tenManService.getLockedDateOptionId(context.channelId());
        if (lockedIdOpt.isPresent() && cooldownService.isOnCooldown(context.userId(), lockedIdOpt.get())) {
            long secs = cooldownService.remainingCooldown(context.userId(), lockedIdOpt.get()).getSeconds();
            long mins = secs / 60;
            long rem = secs % 60;
            String timeLeft = mins > 0 ? mins + "m " + rem + "s" : secs + "s";
            return new TenManFillErrorEvent(context, "Easy there! You recently changed your sign-up. Wait " + timeLeft + " before trying again.");
        }

        TenManFillSlashResult result = tenManService.fillRosterSlot(context.channelId(), context.userId());

        return switch (result) {
            case TenManFillSlashResult.NoPoll np ->
                    new TenManFillErrorEvent(context, "There are no ten-man slots available to fill in this channel.");
            case TenManFillSlashResult.AlreadyOnRoster a ->
                    new TenManFillErrorEvent(context, "You are already on the ten-man roster.");
            case TenManFillSlashResult.NoSlots ns ->
                    new TenManFillErrorEvent(context, "The ten-man roster is already full — no slots to fill.");
            case TenManFillSlashResult.Filled f -> {
                cooldownService.recordAction(context.userId(), f.dateOptionId());
                yield new TenManFillSuccessEvent(context, f.rosterRestored(), context.userId(), context.channelId(),
                        f.dateOptionId(), f.date(), f.timeDisplay(), f.slotsNeeded(), f.rosterUserIds());
            }
        };
    }
}
