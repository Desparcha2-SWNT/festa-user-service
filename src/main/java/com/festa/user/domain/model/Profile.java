package com.festa.user.domain.model;

import com.festa.user.domain.vo.AccountId;

import java.time.Instant;

/**
 * Perfil extendido del usuario (RF2, parte no cubierta por auth).
 *
 * Notas:
 *  - email NO vive aquí: auth es la fuente de verdad.
 *  - displayName se copia de account.registered porque lo exige el contrato profile.updated.
 *  - displayName y phone están en plaintext en el dominio; se cifran AES-256-GCM en persistencia.
 *  - bio, avatarUrl y phone son opcionales.
 */
public record Profile(
        AccountId accountId,
        String displayName,
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

    public static Profile emptyFor(AccountId accountId, String displayName, Instant now) {
        return new Profile(accountId, displayName, null, null, null, now, now);
    }

    public Profile applyUpdate(
            String newBio,
            String newAvatarUrl,
            String newPhone,
            Instant now
    ) {
        return new Profile(accountId, displayName, newBio, newAvatarUrl, newPhone, createdAt, now);
    }
}