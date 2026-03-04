package io.github.jnicog.discord.spanner.bot.command.handler;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManInvalidDateRangeEvent;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManPollAlreadyActiveEvent;
import io.github.jnicog.discord.spanner.bot.event.tenman.TenManPollCreatedEvent;
import io.github.jnicog.discord.spanner.bot.tenman.TenManPollCreatedResult;
import io.github.jnicog.discord.spanner.bot.tenman.TenManService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class TenManCommandHandler implements SlashCommandHandler {

    private static final int MAX_RANGE_DAYS = 14;

    private final TenManService tenManService;

    public TenManCommandHandler(TenManService tenManService) {
        this.tenManService = tenManService;
    }

    @Override
    public String getCommandName() {
        return "ten-man";
    }

    @Override
    public AbstractCommandResult<?> handleCommand(SlashCommandContext context) {
        String startDateStr = context.options().get("start_date");
        String endDateStr = context.options().get("end_date");

        LocalDate startDate;
        LocalDate endDate;
        try {
            startDate = LocalDate.parse(startDateStr);
            endDate = LocalDate.parse(endDateStr);
        } catch (DateTimeParseException e) {
            return new TenManInvalidDateRangeEvent(context,
                    "Invalid date format. Use yyyy-MM-dd (e.g. 2026-03-06).");
        }

        if (startDate.isAfter(endDate)) {
            return new TenManInvalidDateRangeEvent(context,
                    "Start date must be on or before end date.");
        }

        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        if (daysBetween > MAX_RANGE_DAYS) {
            return new TenManInvalidDateRangeEvent(context,
                    "Date range must be 14 days or fewer.");
        }

        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            dates.add(current);
            current = current.plusDays(1);
        }

        String timeDisplay = context.options().get("time");
        boolean testMode = Boolean.parseBoolean(context.options().getOrDefault("test", "false"));

        String durationStr = context.options().get("duration");
        int durationHours = 24;
        if (durationStr != null && !durationStr.isBlank()) {
            try {
                durationHours = Integer.parseInt(durationStr);
            } catch (NumberFormatException ignored) {
            }
        }
        OffsetDateTime closesAt = OffsetDateTime.now().plusHours(durationHours);

        Optional<TenManPollCreatedResult> result = tenManService.createPoll(
                context.channelId(), context.userId(), dates, timeDisplay, testMode, closesAt);

        if (result.isEmpty()) {
            return new TenManPollAlreadyActiveEvent(context, context.channelId());
        }

        TenManPollCreatedResult created = result.get();
        return new TenManPollCreatedEvent(context, created.pollId(), context.channelId(), created.timeDisplay(), created.testMode(), created.closesAt(), created.dateOptions());
    }
}
