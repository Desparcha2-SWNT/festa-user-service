package com.festa.user.domain.event.inbound;

import com.festa.user.domain.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Contrato account.registered (lo publica auth). user-service crea el perfil vacío
 * y guarda el displayName, que necesita para publicar profile.updated.
 */
public record AccountRegisteredEvent(
        UUID eventId,
        Instant occurredAt,
        String eventType,
        UUID accountId,
        String displayName,
        String role,
        Instant registeredAt
) implements DomainEvent {
}
