package com.festa.user.domain.event.outbound;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.festa.user.domain.event.DomainEvent;
import com.festa.user.domain.model.MerchantProfile;

import java.time.Instant;
import java.util.UUID;

/**
 * Contrato merchant.submitted: el comercio envió (o reenvió) su perfil a verificación.
 * Hay un comercio por cuenta, así que merchantId = userId.
 */
public record MerchantSubmittedEvent(
        @JsonIgnore UUID eventId,
        @JsonIgnore Instant occurredAt,
        @JsonIgnore String eventType,
        UUID merchantId,
        UUID userId,
        String businessName,
        String category,
        Instant submittedAt
) implements DomainEvent {

    public static final String TYPE = "merchant.submitted";

    public static MerchantSubmittedEvent of(MerchantProfile m, Instant now) {
        UUID id = m.accountId().value();
        return new MerchantSubmittedEvent(
                UUID.randomUUID(), now, TYPE, id, id, m.businessName(),
                m.category().name().toLowerCase(), m.updatedAt()
        );
    }
}
