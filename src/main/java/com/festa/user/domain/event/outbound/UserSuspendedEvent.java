package com.festa.user.domain.event.outbound;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.festa.user.domain.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/** Contrato moderation.user_suspended (festa-infra/contracts/events/user/moderation.user_suspended.schema.json). RF14. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserSuspendedEvent(
        @JsonIgnore UUID eventId,
        @JsonIgnore Instant occurredAt,
        @JsonIgnore String eventType,
        UUID userId,
        UUID reportId,
        String reason,
        Instant until,
        UUID decidedBy,
        Instant decidedAt
) implements DomainEvent {

    public static final String TYPE = "moderation.user_suspended";

    public static UserSuspendedEvent of(UUID userId, UUID reportId, String reason, Instant until, UUID decidedBy, Instant now) {
        return new UserSuspendedEvent(UUID.randomUUID(), now, TYPE, userId, reportId, reason, until, decidedBy, now);
    }
}
