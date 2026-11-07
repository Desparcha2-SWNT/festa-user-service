package com.festa.user.domain.event.outbound;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.festa.user.domain.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/** Contrato location_consent.revoked (RNF5): los consumidores borran la ubicación guardada. */
public record LocationConsentRevokedEvent(
        @JsonIgnore UUID eventId,
        @JsonIgnore Instant occurredAt,
        @JsonIgnore String eventType,
        UUID userId,
        Instant revokedAt
) implements DomainEvent {

    public static final String TYPE = "location_consent.revoked";

    public static LocationConsentRevokedEvent of(UUID userId, Instant revokedAt, Instant now) {
        return new LocationConsentRevokedEvent(UUID.randomUUID(), now, TYPE, userId, revokedAt);
    }
}
