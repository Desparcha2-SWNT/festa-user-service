package com.festa.user.domain.vo;

import java.util.Objects;
import java.util.UUID;

public record AccountId(UUID value) {

    public AccountId {
        Objects.requireNonNull(value, "value");
    }

    public static AccountId of(UUID value) {
        return new AccountId(value);
    }

    public static AccountId of(String raw) {
        return new AccountId(UUID.fromString(raw));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}