CREATE TABLE user_interests (
    account_id   UUID NOT NULL,
    interest     VARCHAR(32) NOT NULL,
    PRIMARY KEY (account_id, interest),
    CONSTRAINT fk_user_interests_profile
        FOREIGN KEY (account_id) REFERENCES profiles(account_id) ON DELETE CASCADE,
    CONSTRAINT chk_user_interests_value
        CHECK (interest IN ('CULTURAL','MUSICAL','DEPORTIVO','GASTRONOMICO'))
);

CREATE INDEX idx_user_interests_interest ON user_interests(interest);