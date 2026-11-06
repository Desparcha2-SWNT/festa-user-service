package com.festa.user.domain.model;

import com.festa.user.domain.vo.AccountId;

import java.time.Instant;

/**
 * Perfil extendido del usuario (RF2, parte no cubierta por auth).
 *
 * Notas:
 *  - email y displayName NO viven aquí: auth es la fuente de verdad.
 *  - phone está en plaintext en el dominio; se cifra AES-256-GCM en persistencia.
 *  - bio, avatarUrl y phone son opcionales.
 */
public record Profile(
        AccountId accountId,
        String bio,
        String avatarUrl,
        String phone,
        Instant createdAt,
        Instant updatedAt
) {

    public Profile {
        if (accountId == null) {
            throw new IllegalArgumentException("accountId es obligatorio");
        }
        if (createdAt == null || updatedAt == null) {
            throw new IllegalArgumentException("createdAt y updatedAt son obligatorios");
        }
    }

    public static Profile emptyFor(AccountId accountId, Instant now) {
        return new Profile(accountId, null, null, null, now, now);
    }

    public Profile applyUpdate(
            String newBio,
            String newAvatarUrl,
            String newPhone,
            Instant now
    ) {
        return new Profile(accountId, newBio, newAvatarUrl, newPhone, createdAt, now);
    }
}