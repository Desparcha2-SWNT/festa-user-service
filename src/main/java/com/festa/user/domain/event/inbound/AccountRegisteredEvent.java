package com.festa.user.domain.event.inbound;

import com.festa.user.domain.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Publicado por auth al registrarse una cuenta.
 * Payload mínimo para que user-service cree un Profile vacío.
 *
 * accountType: "user" | "merchant" (lo que eligió el usuario al registrarse).
 * role: siempre "user" al registrarse; los merchants se promueven al aprobarse (RF3).
 */
public record AccountRegisteredEvent(
        UUID eventId,
        Instant occurredAt,
        String eventType,
        UUID accountId,
        String role,
        String accountType,
        Instant registeredAt
) implements DomainEvent {
}