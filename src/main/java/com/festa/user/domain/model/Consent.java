package com.festa.user.domain.model;

import com.festa.user.domain.vo.AccountId;
import com.festa.user.domain.vo.ConsentType;

import java.time.Instant;

/**
 * Consentimiento granular (Ley 1581, Circular SIC 002/2024).
 * Se modela como un evento inmutable; el repositorio guarda el histórico
 * y expone el último por (accountId, type).
 *
 * ipHash: HMAC del IP de origen, solo para auditoría, nunca el IP crudo.
 */
public record Consent(
        AccountId accountId,
        ConsentType type,
        boolean granted,
        String version,
        Instant grantedAt,
        String ipHash
) {

    public Consent {
        if (accountId == null) {
            throw new IllegalArgumentException("accountId es obligatorio");
        }
        if (type == null) {
            throw new IllegalArgumentException("type es obligatorio");
        }
        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException("version es obligatoria");
        }
        if (grantedAt == null) {
            throw new IllegalArgumentException("grantedAt es obligatorio");
        }
    }

    public static Consent record(
            AccountId accountId,
            ConsentType type,
            boolean granted,
            String version,
            String ipHash,
            Instant now
    ) {
        return new Consent(accountId, type, granted, version, now, ipHash);
    }
}