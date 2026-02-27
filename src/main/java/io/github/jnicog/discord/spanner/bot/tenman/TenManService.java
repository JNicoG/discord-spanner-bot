package io.github.jnicog.discord.spanner.bot.tenman;

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
     * Cancels the active poll in the channel.
     * Returns the discordMessageId of the cancelled poll (to allow message deletion), or empty if no active poll.
     */
    Optional<Long> cancelPoll(long channelId);

    /**
     * Finds all active polls whose closes_at is in the past, marks them CLOSED, and returns their snapshots.
     */
    List<TenManPollSnapshot> processExpiredPolls();
}
