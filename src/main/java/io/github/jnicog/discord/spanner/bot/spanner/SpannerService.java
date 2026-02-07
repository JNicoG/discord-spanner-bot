package io.github.jnicog.discord.spanner.bot.spanner;

/**
 * Service interface for spanner operations.
 * Provides a decoupled abstraction over the repository layer.
 */
public interface SpannerService {

    /**
     * Increments the spanner count for a user in a channel.
     *
     * @param userId    The Discord user ID
     * @param channelId The Discord channel ID
     */
    void incrementSpannerCount(long userId, long channelId);

    /**
     * Gets the spanner count for a user in a specific channel.
     *
     * @param userId    The Discord user ID
     * @param channelId The Discord channel ID
     * @return The spanner count, or 0 if no record exists
     */
    int getSpannerCount(long userId, long channelId);

    /**
     * Gets the total spanner count for a user across all channels.
     *
     * @param userId The Discord user ID
     * @return The total spanner count
     */
    int getTotalSpannerCount(long userId);
}
