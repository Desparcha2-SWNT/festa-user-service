package com.festa.user.domain.event.outbound;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.festa.user.domain.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/** Contrato report.resolved (festa-infra/contracts/events/user/report.resolved.schema.json). RF14. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReportResolvedEvent(
        @JsonIgnore UUID eventId,
        @JsonIgnore Instant occurredAt,
        @JsonIgnore String eventType,
        UUID reportId,
        UUID reporterId,
        String targetType,
        UUID targetId,
        String outcome,
        UUID resolvedBy,
        Instant resolvedAt
) implements DomainEvent {

    public static final String TYPE = "report.resolved";

    public static ReportResolvedEvent of(UUID reportId, UUID reporterId, String targetType, UUID targetId, String outcome, UUID resolvedBy, Instant now) {
        return new ReportResolvedEvent(UUID.randomUUID(), now, TYPE, reportId, reporterId, targetType, targetId, outcome, resolvedBy, now);
    }
}
