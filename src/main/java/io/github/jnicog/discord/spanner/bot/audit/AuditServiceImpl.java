package io.github.jnicog.discord.spanner.bot.audit;

import io.github.jnicog.discord.spanner.bot.repository.AuditEventRepository;
import io.github.jnicog.discord.spanner.bot.repository.entity.AuditEventEntity;
import io.github.jnicog.discord.spanner.bot.repository.entity.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Implementation of AuditService using JPA repository.
 * Records audit events asynchronously to avoid blocking the main flow.
 */
@Service
public class AuditServiceImpl implements AuditService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditServiceImpl.class);

    private final AuditEventRepository auditEventRepository;

    public AuditServiceImpl(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @Override
    @Async
    @Transactional
    public void recordEvent(long channelId, long userId, EventType eventType, Map<String, Object> eventData) {
        AuditEventEntity event = new AuditEventEntity(channelId, userId, eventType, eventData);
        auditEventRepository.save(event);
        LOGGER.debug("Recorded audit event: {} for user {} in channel {}", eventType, userId, channelId);
    }

    @Override
    @Async
    @Transactional
    public void recordEvent(long channelId, EventType eventType, Map<String, Object> eventData) {
        AuditEventEntity event = new AuditEventEntity(channelId, eventType, eventData);
        auditEventRepository.save(event);
        LOGGER.debug("Recorded audit event: {} in channel {}", eventType, channelId);
    }

    @Override
    @Async
    @Transactional
    public void recordEvent(long channelId, long userId, EventType eventType) {
        recordEvent(channelId, userId, eventType, null);
    }
}

