CREATE TABLE profiles (
    account_id   UUID PRIMARY KEY,
    bio          TEXT,
    avatar_url   TEXT,
    phone_enc    TEXT,          -- AES-256-GCM (base64)
    created_at   TIMESTAMPTZ NOT NULL,
    updated_at   TIMESTAMPTZ NOT NULL
);