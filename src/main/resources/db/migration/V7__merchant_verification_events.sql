CREATE TABLE merchant_verification_events (
    id           UUID PRIMARY KEY,
    account_id   UUID NOT NULL,
    from_status  VARCHAR(16),
    to_status    VARCHAR(16) NOT NULL,
    actor_id     UUID,
    reason       TEXT,
    created_at   TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_mve_merchant
        FOREIGN KEY (account_id) REFERENCES merchant_profiles(account_id) ON DELETE CASCADE,
    CONSTRAINT chk_mve_to_status
        CHECK (to_status IN ('PENDING','VERIFIED','REJECTED')),
    CONSTRAINT chk_mve_from_status
        CHECK (from_status IS NULL OR from_status IN ('PENDING','VERIFIED','REJECTED'))
);

CREATE INDEX idx_mve_account_at ON merchant_verification_events(account_id, created_at DESC);