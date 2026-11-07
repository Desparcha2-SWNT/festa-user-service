package com.festa.user.domain.event.outbound;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.festa.user.domain.event.DomainEvent;
import com.festa.user.domain.model.MerchantProfile;

import java.time.Instant;
import java.util.UUID;

/** Contrato merchant.rejected: el motivo va en español, mostrable al comercio. */
public record MerchantRejectedEvent(
        @JsonIgnore UUID eventId,
        @JsonIgnore Instant occurredAt,
        @JsonIgnore String eventType,
        UUID merchantId,
        UUID userId,
        String reason,
        UUID rejectedBy,
        Instant rejectedAt
) implements DomainEvent {

    public static final String TYPE = "merchant.rejected";

    public static MerchantRejectedEvent of(MerchantProfile m, String reason, UUID rejectedBy, Instant now) {
        UUID id = m.accountId().value();
        return new MerchantRejectedEvent(UUID.randomUUID(), now, TYPE, id, id, reason, rejectedBy, m.updatedAt());
    }
}
