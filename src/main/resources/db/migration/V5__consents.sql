CREATE TABLE consents (
    id          UUID PRIMARY KEY,
    account_id  UUID NOT NULL,
    type        VARCHAR(16) NOT NULL,
    granted     BOOLEAN NOT NULL,
    version     VARCHAR(32) NOT NULL,
    granted_at  TIMESTAMPTZ NOT NULL,
    ip_hash     VARCHAR(128),
    CONSTRAINT fk_consents_profile
        FOREIGN KEY (account_id) REFERENCES profiles(account_id) ON DELETE CASCADE,
    CONSTRAINT chk_consents_type
        CHECK (type IN ('TERMS','PRIVACY','LOCATION','NOTIFICATIONS'))
);

CREATE INDEX idx_consents_account_type_at
    ON consents(account_id, type, granted_at DESC);