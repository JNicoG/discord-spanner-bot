package io.github.jnicog.discord.spanner.bot.tenman;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface TenManService {

    /**
     * Creates a new poll. Returns empty if a poll is already active in the channel.
     */
    Optional<TenManPollCreatedResult> createPoll(long channelId, long createdByUserId, List<LocalDate> dates, String timeDisplay, boolean testMode, OffsetDateTime closesAt);

    TenManSignupToggleResult toggleSignup(long dateOptionId, long userId);

    void registerMessageId(long pollId, long discordMessageId);

    Optional<TenManPollSnapshot> getActivePollByMessageId(long discordMessageId);

    TenManPollSnapshot getPollSnapshot(long pollId);

    /**
     * Cancels the active or locked poll in the channel.
     * Returns the discordMessageId of the cancelled poll (to allow message deletion), or empty if no poll found.
     */
    Optional<Long> cancelPoll(long channelId);

    /**
     * Locks the poll (a date has reached capacity). Records which date option was the trigger.
     */
    void lockPoll(long pollId, long lockedDateOptionId);

    /**
     * Returns the locked date option ID for the channel's locked poll, or empty if no locked poll exists.
     * Used for cooldown checks before resign/fill slash commands execute.
     */
    Optional<Long> getLockedDateOptionId(long channelId);

    /**
     * Resign from the locked roster via slash command.
     */
    TenManResignSlashResult resignFromLockedRoster(long channelId, long userId);

    /**
     * Fill an open slot in the locked roster via slash command.
     */
    TenManFillSlashResult fillRosterSlot(long channelId, long userId);

    /**
     * Finds all active polls whose closes_at is in the past, marks them CLOSED, and returns their snapshots.
     */
    List<TenManPollSnapshot> processExpiredPolls();

    /**
     * Returns snapshots of ACTIVE polls whose closes_at falls within the next {@code window} duration.
     * Used to fire expiry warning notifications.
     */
    List<TenManPollSnapshot> getPollsClosingSoon(Duration window);

    /**
     * Returns the snapshot of the active or locked poll in the channel, or empty if none.
     */
    Optional<TenManPollSnapshot> getActivePollSnapshot(long channelId);
}
