package com.festa.user.domain.event.outbound;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.festa.user.domain.event.DomainEvent;
import com.festa.user.domain.model.MerchantProfile;

import java.time.Instant;
import java.util.UUID;

/**
 * Contrato merchant.verified: un admin aprobó el comercio (RF3). event-service habilita
 * la boletería y payment lo registra para liquidaciones.
 */
public record MerchantVerifiedEvent(
        @JsonIgnore UUID eventId,
        @JsonIgnore Instant occurredAt,
        @JsonIgnore String eventType,
        UUID merchantId,
        UUID userId,
        String businessName,
        String category,
        MerchantPlace place,
        UUID verifiedBy,
        Instant verifiedAt
) implements DomainEvent {

    public static final String TYPE = "merchant.verified";

    public static MerchantVerifiedEvent of(MerchantProfile m, UUID verifiedBy, Instant now) {
        UUID id = m.accountId().value();
        return new MerchantVerifiedEvent(
                UUID.randomUUID(), now, TYPE, id, id, m.businessName(),
                m.category().name().toLowerCase(), MerchantPlace.of(m), verifiedBy, m.updatedAt()
        );
    }
}
