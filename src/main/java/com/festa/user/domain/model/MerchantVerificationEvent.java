package com.festa.user.domain.model;

import com.festa.user.domain.vo.AccountId;
import com.festa.user.domain.vo.MerchantStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Registro de auditoría de cada transición de verificación (RF3, revisión manual).
 * Se guarda en merchant_verification_events. No se publica a RabbitMQ.
 */
public record MerchantVerificationEvent(
        UUID id,
        AccountId accountId,
        MerchantStatus fromStatus,
        MerchantStatus toStatus,
        AccountId actorId,
        String reason,
        Instant createdAt
) {

    public MerchantVerificationEvent {
        if (id == null) {
            throw new IllegalArgumentException("id es obligatorio");
        }
        if (accountId == null) {
            throw new IllegalArgumentException("accountId es obligatorio");
        }
        if (toStatus == null) {
            throw new IllegalArgumentException("toStatus es obligatorio");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt es obligatorio");
        }
    }

    public static MerchantVerificationEvent of(
            AccountId accountId,
            MerchantStatus fromStatus,
            MerchantStatus toStatus,
            AccountId actorId,
            String reason,
            Instant now
    ) {
        return new MerchantVerificationEvent(
                UUID.randomUUID(), accountId, fromStatus, toStatus, actorId, reason, now
        );
    }
}