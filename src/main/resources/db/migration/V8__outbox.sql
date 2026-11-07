CREATE TABLE outbox (
    id           UUID PRIMARY KEY,
    event_type   VARCHAR(64) NOT NULL,
    payload_json JSONB       NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    published_at TIMESTAMPTZ
);

CREATE INDEX idx_outbox_pending ON outbox(created_at)
    WHERE published_at IS NULL;