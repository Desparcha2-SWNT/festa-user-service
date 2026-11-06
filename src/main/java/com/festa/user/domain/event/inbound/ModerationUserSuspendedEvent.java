package com.festa.user.domain.event.inbound;

import com.festa.user.domain.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Publicado por el servicio de moderación (o auth) al suspender una cuenta.
 * user-service oculta la ficha pública del comercio (si existe).
 */
public record ModerationUserSuspendedEvent(
        UUID eventId,
        Instant occurredAt,
        String eventType,
        UUID accountId,
        String reason
) implements DomainEvent {
}