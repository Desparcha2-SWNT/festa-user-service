package com.festa.user.domain.event.inbound;

import com.festa.user.domain.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Publicado por auth tras anonimizar la cuenta (Ley 1581).
 * user-service borra en duro su propia PII (profile, interests, preferences,
 * consents, merchant profile + audit events).
 */
public record AccountDeletedEvent(
        UUID eventId,
        Instant occurredAt,
        String eventType,
        UUID accountId
) implements DomainEvent {
}