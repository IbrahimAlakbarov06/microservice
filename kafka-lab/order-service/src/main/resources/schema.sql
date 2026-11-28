CREATE TABLE IF NOT EXISTS order_event (
    id              BIGSERIAL PRIMARY KEY,
    event_id        TEXT        NOT NULL,
    customer        TEXT        NOT NULL,
    type            TEXT        NOT NULL,
    seq             INT         NOT NULL,
    topic_partition INT         NOT NULL,
    topic_offset    BIGINT      NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS processed_event (
    event_id TEXT PRIMARY KEY
);
