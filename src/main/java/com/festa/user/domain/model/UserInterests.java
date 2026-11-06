package com.festa.user.domain.model;

import com.festa.user.domain.vo.AccountId;
import com.festa.user.domain.vo.Interest;

import java.time.Instant;
import java.util.Set;

/**
 * Intereses de plan del usuario (RF2). Se reemplazan como conjunto completo
 * en cada PUT /users/me/interests.
 */
public record UserInterests(
        AccountId accountId,
        Set<Interest> interests,
        Instant updatedAt
) {

    public UserInterests {
        if (accountId == null) {
            throw new IllegalArgumentException("accountId es obligatorio");
        }
        interests = interests == null ? Set.of() : Set.copyOf(interests);
        if (updatedAt == null) {
            throw new IllegalArgumentException("updatedAt es obligatorio");
        }
    }

    public static UserInterests empty(AccountId accountId, Instant now) {
        return new UserInterests(accountId, Set.of(), now);
    }

    public UserInterests replace(Set<Interest> newInterests, Instant now) {
        return new UserInterests(accountId, newInterests, now);
    }
}