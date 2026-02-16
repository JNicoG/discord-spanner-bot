package io.github.jnicog.discord.spanner.bot.audit;

import io.github.jnicog.discord.spanner.bot.repository.entity.EventType;

import java.util.Map;

/**
 * Service interface for audit event operations.
 * Provides a decoupled abstraction for recording audit events.
 */
public interface AuditService {

    /**
     * Records an audit event with a user.
     *
     * @param channelId The Discord channel ID
     * @param userId    The Discord user ID
     * @param eventType The type of event
     * @param eventData Additional event-specific data
     */
    void recordEvent(long channelId, long userId, EventType eventType, Map<String, Object> eventData);

    /**
     * Records an audit event without a specific user.
     *
     * @param channelId The Discord channel ID
     * @param eventType The type of event
     * @param eventData Additional event-specific data
     */
    void recordEvent(long channelId, EventType eventType, Map<String, Object> eventData);

    /**
     * Records an audit event with a user and no additional data.
     *
     * @param channelId The Discord channel ID
     * @param userId    The Discord user ID
     * @param eventType The type of event
     */
    void recordEvent(long channelId, long userId, EventType eventType);
}

