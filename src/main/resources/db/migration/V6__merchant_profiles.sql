CREATE TABLE merchant_profiles (
    account_id           UUID PRIMARY KEY,
    business_name        VARCHAR(120) NOT NULL,
    category             VARCHAR(32)  NOT NULL,
    address_enc          TEXT         NOT NULL,  -- AES-256-GCM (base64)
    latitude             DOUBLE PRECISION NOT NULL,
    longitude            DOUBLE PRECISION NOT NULL,
    description          TEXT,
    verification_status  VARCHAR(16) NOT NULL,
    suspended            BOOLEAN NOT NULL DEFAULT FALSE,
    rejection_reason     TEXT,
    created_at           TIMESTAMPTZ NOT NULL,
    updated_at           TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_merchant_profiles_profile
        FOREIGN KEY (account_id) REFERENCES profiles(account_id) ON DELETE CASCADE,
    CONSTRAINT chk_merchant_profiles_category
        CHECK (category IN ('CULTURAL','MUSICAL','DEPORTIVO','GASTRONOMICO','OTRO')),
    CONSTRAINT chk_merchant_profiles_status
        CHECK (verification_status IN ('PENDING','VERIFIED','REJECTED')),
    CONSTRAINT chk_merchant_profiles_lat
        CHECK (latitude  BETWEEN -90 AND 90),
    CONSTRAINT chk_merchant_profiles_lng
        CHECK (longitude BETWEEN -180 AND 180)
);

CREATE INDEX idx_merchant_profiles_status ON merchant_profiles(verification_status);
CREATE INDEX idx_merchant_profiles_public
    ON merchant_profiles(verification_status, suspended);