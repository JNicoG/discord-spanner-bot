-- V1__Create_spanner_table.sql
-- Initial schema for the spanner tracking system

CREATE TABLE IF NOT EXISTS spanner (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    channel_id BIGINT NOT NULL,
    spanner_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_spanner_user_channel UNIQUE (user_id, channel_id)
);

CREATE INDEX idx_spanner_user_id ON spanner(user_id);
CREATE INDEX idx_spanner_channel_id ON spanner(channel_id);

COMMENT ON TABLE spanner IS 'Tracks spanner counts for users per channel';
COMMENT ON COLUMN spanner.user_id IS 'Discord user ID';
COMMENT ON COLUMN spanner.channel_id IS 'Discord channel ID';
COMMENT ON COLUMN spanner.spanner_count IS 'Number of times user has spannered in this channel';

