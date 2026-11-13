package com.festa.user.domain.event.outbound;

import com.fasterxml.jackson.annotation.JsonValue;
import com.festa.user.domain.event.DomainEvent;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Contrato moderation.event_restored. RF14.
 * El `data` del contrato tiene un campo `eventId` (el evento moderado) que choca con
 * {@link DomainEvent#eventId()} (el id del mensaje, que el outbox usa como clave). Por eso el
 * evento moderado es {@code targetEventId} y el `data` se arma explícitamente con {@link JsonValue}.
 */
public record EventRestoredEvent(
        UUID eventId,
        Instant occurredAt,
        String eventType,
        UUID targetEventId,
        UUID decidedBy,
        Instant decidedAt
) implements DomainEvent {

    public static final String TYPE = "moderation.event_restored";

    public static EventRestoredEvent of(UUID targetEventId, UUID decidedBy, Instant now) {
        return new EventRestoredEvent(UUID.randomUUID(), now, TYPE, targetEventId, decidedBy, now);
    }

    @JsonValue
    public Map<String, Object> data() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("eventId", targetEventId);
        data.put("decidedBy", decidedBy);
        data.put("decidedAt", decidedAt);
        return data;
    }
}
