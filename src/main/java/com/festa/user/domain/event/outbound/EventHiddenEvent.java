package com.festa.user.domain.event.outbound;

import com.fasterxml.jackson.annotation.JsonValue;
import com.festa.user.domain.event.DomainEvent;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Contrato moderation.event_hidden. RF14.
 * El `data` del contrato tiene un campo `eventId` (el evento moderado) que choca con
 * {@link DomainEvent#eventId()} (el id del mensaje, que el outbox usa como clave). Por eso el
 * evento moderado es {@code targetEventId} y el `data` se arma explícitamente con {@link JsonValue}.
 */
public record EventHiddenEvent(
        UUID eventId,
        Instant occurredAt,
        String eventType,
        UUID targetEventId,
        UUID reportId,
        String reason,
        UUID decidedBy,
        Instant decidedAt
) implements DomainEvent {

    public static final String TYPE = "moderation.event_hidden";

    public static EventHiddenEvent of(UUID targetEventId, UUID reportId, String reason, UUID decidedBy, Instant now) {
        return new EventHiddenEvent(UUID.randomUUID(), now, TYPE, targetEventId, reportId, reason, decidedBy, now);
    }

    @JsonValue
    public Map<String, Object> data() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("eventId", targetEventId);
        if (reportId != null) {
            data.put("reportId", reportId);
        }
        data.put("reason", reason);
        data.put("decidedBy", decidedBy);
        data.put("decidedAt", decidedAt);
        return data;
    }
}
