package io.github.jnicog.discord.spanner.bot.command.handler;

import io.github.jnicog.discord.spanner.bot.command.ButtonInteractionContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManDateFullEvent;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManSignupToggledEvent;
import io.github.jnicog.discord.spanner.bot.tenman.TenManDateOptionSnapshot;
import io.github.jnicog.discord.spanner.bot.tenman.TenManPollSnapshot;
import io.github.jnicog.discord.spanner.bot.tenman.TenManService;
import io.github.jnicog.discord.spanner.bot.tenman.TenManSignupToggleResult;
import org.springframework.stereotype.Component;

@Component
public class TenManSignupButtonHandler implements ButtonCommandHandler {

    private static final String BUTTON_PREFIX = "tenman_";

    private final TenManService tenManService;

    public TenManSignupButtonHandler(TenManService tenManService) {
        this.tenManService = tenManService;
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

        TenManSignupToggleResult toggleResult = tenManService.toggleSignup(dateOptionId, userId);
        TenManPollSnapshot snapshot = tenManService.getPollSnapshot(toggleResult.pollId());

        if (toggleResult.dateFull()) {
            TenManDateOptionSnapshot fullOption = snapshot.dateOptions().stream()
                    .filter(o -> o.id() == dateOptionId)
                    .findFirst()
                    .orElseThrow();
            return new TenManDateFullEvent(
                    context,
                    toggleResult.pollId(),
                    snapshot.channelId(),
                    dateOptionId,
                    fullOption.date(),
                    fullOption.signedUpUserIds(),
                    snapshot
            );
        }

        return new TenManSignupToggledEvent(
                context,
                toggleResult.pollId(),
                snapshot.channelId(),
                snapshot.discordMessageId(),
                userId,
                toggleResult.added(),
                dateOptionId,
                snapshot
        );
    }

    private long parseOptionId(String componentId) {
        try {
            return Long.parseLong(componentId.substring(BUTTON_PREFIX.length()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid ten-man button component ID: " + componentId, e);
        }
    }
}
