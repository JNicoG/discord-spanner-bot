-- V2__Create_audit_event_table.sql
-- Audit trail for tracking all events that occur in channels

-- Enum type for event categories
CREATE TYPE event_type AS ENUM (
    -- Queue events
    'PLAYER_JOINED_QUEUE',
    'PLAYER_LEFT_QUEUE',
    'PLAYER_QUEUE_TIMEOUT',

    -- Check-in events
    'CHECK_IN_STARTED',
    'PLAYER_CHECKED_IN',
    'CHECK_IN_COMPLETED',
    'CHECK_IN_CANCELLED',
    'CHECK_IN_TIMEOUT',

    -- Spanner events
    'SPANNER_AWARDED'
);

CREATE TABLE IF NOT EXISTS audit_event (
    id BIGSERIAL PRIMARY KEY,
    channel_id BIGINT NOT NULL,
    user_id BIGINT,  -- Nullable: some events may not be user-specific
    event_type event_type NOT NULL,
    event_data JSONB,  -- Type-specific fields stored as JSON
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for common query patterns
CREATE INDEX idx_audit_event_channel_id ON audit_event(channel_id);
CREATE INDEX idx_audit_event_user_id ON audit_event(user_id) WHERE user_id IS NOT NULL;
CREATE INDEX idx_audit_event_type ON audit_event(event_type);
CREATE INDEX idx_audit_event_occurred_at ON audit_event(occurred_at);
CREATE INDEX idx_audit_event_channel_occurred ON audit_event(channel_id, occurred_at DESC);

-- GIN index for JSONB queries
CREATE INDEX idx_audit_event_data ON audit_event USING GIN (event_data);

COMMENT ON TABLE audit_event IS 'Audit trail of all events that occur in channels';
COMMENT ON COLUMN audit_event.channel_id IS 'Discord channel ID where the event occurred';
COMMENT ON COLUMN audit_event.user_id IS 'Discord user ID associated with the event (if applicable)';
COMMENT ON COLUMN audit_event.event_type IS 'Type/category of the event';
COMMENT ON COLUMN audit_event.event_data IS 'Type-specific event data stored as JSON';
COMMENT ON COLUMN audit_event.occurred_at IS 'Timestamp when the event occurred';

/*
Event Data Examples:

PLAYER_JOINED_QUEUE:
{
    "queue_size_after": 3,
    "max_queue_size": 5
}

CHECK_IN_STARTED:
{
    "participants": [123456789, 987654321, ...],
    "timeout_seconds": 300
}

CHECK_IN_CANCELLED:
{
    "cancelled_by_user_id": 123456789,
    "remaining_users": [987654321, ...],
    "reason": "BUTTON" | "UNKEEN"
}

CHECK_IN_TIMEOUT:
{
    "users_who_checked_in": [123456789, ...],
    "users_who_did_not_check_in": [987654321, ...]
}

SPANNER_AWARDED:
{
    "reason": "CHECK_IN_CANCELLED" | "CHECK_IN_TIMEOUT" | "LEFT_QUEUE_DURING_CHECK_IN",
    "new_spanner_count": 5
}
*/

