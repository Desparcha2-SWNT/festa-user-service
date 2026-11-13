package com.festa.user.domain.event.outbound;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.festa.user.domain.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/** Contrato report.created (festa-infra/contracts/events/user/report.created.schema.json). RF14. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReportCreatedEvent(
        @JsonIgnore UUID eventId,
        @JsonIgnore Instant occurredAt,
        @JsonIgnore String eventType,
        UUID reportId,
        UUID reporterId,
        String targetType,
        UUID targetId,
        String reason,
        String comment,
        Instant createdAt
) implements DomainEvent {

    public static final String TYPE = "report.created";

    public static ReportCreatedEvent of(UUID reportId, UUID reporterId, String targetType, UUID targetId, String reason, String comment, Instant now) {
        return new ReportCreatedEvent(UUID.randomUUID(), now, TYPE, reportId, reporterId, targetType, targetId, reason, comment, now);
    }
}
