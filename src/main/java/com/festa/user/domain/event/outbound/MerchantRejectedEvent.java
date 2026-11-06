package com.festa.user.domain.event.outbound;

import com.festa.user.domain.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Se publica al rechazar un comercio (RF3). Motivo en español, mostrable
 * al usuario. Analytics lo usa para trazabilidad del proceso manual.
 */
public record MerchantRejectedEvent(
        UUID eventId,
        Instant occurredAt,
        String eventType,
        UUID accountId,
        String businessName,
        String reason,
        UUID rejectedBy,
        Instant rejectedAt
) implements DomainEvent {

    public static final String TYPE = "merchant.rejected";

    public static MerchantRejectedEvent of(
            UUID accountId,
            String businessName,
            String reason,
            UUID rejectedBy,
            Instant rejectedAt,
            Instant now
    ) {
        return new MerchantRejectedEvent(
                UUID.randomUUID(), now, TYPE, accountId, businessName, reason, rejectedBy, rejectedAt
        );
    }
}