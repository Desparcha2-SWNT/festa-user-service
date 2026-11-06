package com.festa.user.domain.event.inbound;

import com.festa.user.domain.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Publicado por auth cuando cambia el rol de una cuenta (p. ej. user -> merchant
 * tras aprobación, o merchant -> user tras rechazo de un comercio ya verificado).
 */
public record AccountRoleChangedEvent(
        UUID eventId,
        Instant occurredAt,
        String eventType,
        UUID accountId,
        String oldRole,
        String newRole
) implements DomainEvent {
}