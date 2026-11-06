package com.festa.user.domain.event.outbound;

import com.festa.user.domain.event.DomainEvent;
import com.festa.user.domain.vo.ConsentType;

import java.time.Instant;
import java.util.UUID;

public record UserConsentUpdatedEvent(
        UUID eventId,
        Instant occurredAt,
        String eventType,
        UUID accountId,
        ConsentType consentType,
        boolean granted,
        String version
) implements DomainEvent {

    public static final String TYPE = "user.consent_updated";

    public static UserConsentUpdatedEvent of(
            UUID accountId,
            ConsentType consentType,
            boolean granted,
            String version,
            Instant now
    ) {
        return new UserConsentUpdatedEvent(
                UUID.randomUUID(), now, TYPE, accountId, consentType, granted, version
        );
    }
}
