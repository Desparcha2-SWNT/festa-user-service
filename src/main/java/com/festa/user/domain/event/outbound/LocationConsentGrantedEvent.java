package com.festa.user.domain.event.outbound;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.festa.user.domain.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/** Contrato location_consent.granted (RNF5): el usuario aceptó el uso de su ubicación. */
public record LocationConsentGrantedEvent(
        @JsonIgnore UUID eventId,
        @JsonIgnore Instant occurredAt,
        @JsonIgnore String eventType,
        UUID userId,
        String policyVersion,
        Instant grantedAt
) implements DomainEvent {

    public static final String TYPE = "location_consent.granted";

    public static LocationConsentGrantedEvent of(UUID userId, String policyVersion, Instant grantedAt, Instant now) {
        return new LocationConsentGrantedEvent(UUID.randomUUID(), now, TYPE, userId, policyVersion, grantedAt);
    }
}
