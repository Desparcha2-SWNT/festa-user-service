CREATE TABLE user_preferences (
    account_id       UUID PRIMARY KEY,
    budget_range     VARCHAR(16) NOT NULL DEFAULT 'ANY',
    max_distance_km  INTEGER,
    updated_at       TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_user_preferences_profile
        FOREIGN KEY (account_id) REFERENCES profiles(account_id) ON DELETE CASCADE,
    CONSTRAINT chk_user_preferences_budget
        CHECK (budget_range IN ('LOW','MEDIUM','HIGH','ANY')),
    CONSTRAINT chk_user_preferences_distance
        CHECK (max_distance_km IS NULL OR (max_distance_km >= 0 AND max_distance_km <= 100))
);

CREATE TABLE user_preferred_zones (
    account_id  UUID NOT NULL,
    zone        VARCHAR(64) NOT NULL,
    PRIMARY KEY (account_id, zone),
    CONSTRAINT fk_user_preferred_zones_pref
        FOREIGN KEY (account_id) REFERENCES user_preferences(account_id) ON DELETE CASCADE
);

CREATE TABLE user_preferred_days (
    account_id  UUID NOT NULL,
    day_of_week VARCHAR(16) NOT NULL,
    PRIMARY KEY (account_id, day_of_week),
    CONSTRAINT fk_user_preferred_days_pref
        FOREIGN KEY (account_id) REFERENCES user_preferences(account_id) ON DELETE CASCADE,
    CONSTRAINT chk_user_preferred_days_value
        CHECK (day_of_week IN ('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY'))
);