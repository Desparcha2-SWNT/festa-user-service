package com.festa.user.domain.event.outbound;

import com.festa.user.domain.event.DomainEvent;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Se publica cuando cambia el perfil extendido (bio/avatar/phone).
 * No lleva el contenido, solo los campos cambiados; los consumidores
 * consultan GET /users/me si necesitan el detalle (no hay PII en el evento).
 */
public record UserProfileUpdatedEvent(
        UUID eventId,
        Instant occurredAt,
        String eventType,
        UUID accountId,
        Set<String> changedFields
) implements DomainEvent {

    public static final String TYPE = "user.profile_updated";

    public static UserProfileUpdatedEvent of(UUID accountId, Set<String> changedFields, Instant now) {
        return new UserProfileUpdatedEvent(
                UUID.randomUUID(), now, TYPE, accountId,
                changedFields == null ? Set.of() : Set.copyOf(changedFields)
        );
    }
}