package io.github.jnicog.discord.spanner.bot.command.handler;

import io.github.jnicog.discord.spanner.bot.command.ButtonInteractionContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManCooldownButtonEvent;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManDateFullEvent;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManLockedPollInteractionEvent;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManRosterResignedButtonEvent;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManSignupToggledEvent;
import io.github.jnicog.discord.spanner.bot.tenman.TenManCooldownService;
import io.github.jnicog.discord.spanner.bot.tenman.TenManDateOptionSnapshot;
import io.github.jnicog.discord.spanner.bot.tenman.TenManPollSnapshot;
import io.github.jnicog.discord.spanner.bot.tenman.TenManService;
import io.github.jnicog.discord.spanner.bot.tenman.TenManSignupToggleResult;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
public class TenManSignupButtonHandler implements ButtonCommandHandler {

    private static final String BUTTON_PREFIX = "tenman_";
    private static final DateTimeFormatter LONG_FORMAT = DateTimeFormatter.ofPattern("EEEE d MMM", Locale.ENGLISH);

    private final TenManService tenManService;
    private final TenManCooldownService cooldownService;

    public TenManSignupButtonHandler(TenManService tenManService, TenManCooldownService cooldownService) {
        this.tenManService = tenManService;
        this.cooldownService = cooldownService;
    }

    @Override
    public String getCommandName() {
        return "tenman";
    }

    @Override
    public boolean matchesComponentId(String componentId) {
        return componentId.startsWith(BUTTON_PREFIX);
    }

    @Override
    public AbstractCommandResult<?> handleCommand(ButtonInteractionContext context) {
        long dateOptionId = parseOptionId(context.componentId());
        long userId = context.userId();

        if (cooldownService.isOnCooldown(userId, dateOptionId)) {
            long seconds = cooldownService.remainingCooldown(userId, dateOptionId).getSeconds();
            return new TenManCooldownButtonEvent(context, userId, Math.max(seconds, 1));
        }

        TenManSignupToggleResult toggleResult = tenManService.toggleSignup(dateOptionId, userId);

        return switch (toggleResult.status()) {
            case POLL_CLOSED -> new TenManLockedPollInteractionEvent(context, "This poll is closed.");

            case WRONG_DATE_FOR_LOCKED_POLL -> {
                TenManPollSnapshot snapshot = tenManService.getPollSnapshot(toggleResult.pollId());
                Long lockedId = snapshot.lockedDateOptionId();
                TenManDateOptionSnapshot lockedOpt = lockedId != null
                        ? snapshot.dateOptions().stream().filter(o -> o.id() == lockedId).findFirst().orElse(null)
                        : null;
                String msg = lockedOpt != null
                        ? "The ten-man is set for " + lockedOpt.date().format(LONG_FORMAT) + ". That date was not selected."
                        : "This date was not selected for the ten-man.";
                yield new TenManLockedPollInteractionEvent(context, msg);
            }

            case NOT_ON_LOCKED_ROSTER ->
                    new TenManLockedPollInteractionEvent(context, "You're not on the roster. Use `/ten-man-fill` to take a slot.");

            case RESIGNED_FROM_LOCKED -> {
                cooldownService.recordAction(userId, dateOptionId);
                TenManPollSnapshot snapshot = tenManService.getPollSnapshot(toggleResult.pollId());
                Long lockedId = snapshot.lockedDateOptionId();
                TenManDateOptionSnapshot lockedOpt = snapshot.dateOptions().stream()
                        .filter(o -> o.id() == lockedId).findFirst().orElseThrow();
                int capacity = snapshot.testMode() ? 1 : 10;
                int slotsNeeded = capacity - lockedOpt.signedUpUserIds().size();
                yield new TenManRosterResignedButtonEvent(context, userId, snapshot.channelId(), snapshot, lockedOpt.date(), slotsNeeded);
            }

            case DATE_FULL -> {
                cooldownService.recordAction(userId, dateOptionId);
                tenManService.lockPoll(toggleResult.pollId(), dateOptionId);
                TenManPollSnapshot snapshot = tenManService.getPollSnapshot(toggleResult.pollId());
                TenManDateOptionSnapshot fullOpt = snapshot.dateOptions().stream()
                        .filter(o -> o.id() == dateOptionId).findFirst().orElseThrow();
                yield new TenManDateFullEvent(context, toggleResult.pollId(), snapshot.channelId(), dateOptionId, fullOpt.date(), fullOpt.signedUpUserIds(), snapshot);
            }

            case JOINED, LEFT -> {
                cooldownService.recordAction(userId, dateOptionId);
                TenManPollSnapshot snapshot = tenManService.getPollSnapshot(toggleResult.pollId());
                yield new TenManSignupToggledEvent(context, toggleResult.pollId(), snapshot.channelId(),
                        snapshot.discordMessageId(), userId,
                        toggleResult.status() == TenManSignupToggleResult.ToggleStatus.JOINED,
                        dateOptionId, snapshot);
            }
        };
    }

    private long parseOptionId(String componentId) {
        try {
            return Long.parseLong(componentId.substring(BUTTON_PREFIX.length()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid ten-man button component ID: " + componentId, e);
        }
    }
}
