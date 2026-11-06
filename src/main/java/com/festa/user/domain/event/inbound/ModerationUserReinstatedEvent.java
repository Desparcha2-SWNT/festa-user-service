package com.festa.user.domain.event.inbound;

import com.festa.user.domain.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Publicado al reintegrar una cuenta suspendida. user-service vuelve a mostrar
 * la ficha pública del comercio si está VERIFIED.
 */
public record ModerationUserReinstatedEvent(
        UUID eventId,
        Instant occurredAt,
        String eventType,
        UUID accountId
) implements DomainEvent {
}