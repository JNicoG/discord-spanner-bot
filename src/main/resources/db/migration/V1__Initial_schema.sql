CREATE TABLE IF NOT EXISTS public.spanners (
    user_id bigint NOT NULL,
    spanner_count integer,
    channel_id bigint NOT NULL,
    CONSTRAINT spanners_pkey PRIMARY KEY (user_id)
);