package com.festa.user.domain.event.outbound;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.festa.user.domain.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/** Contrato moderation.user_reinstated (festa-infra/contracts/events/user/moderation.user_reinstated.schema.json). RF14. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserReinstatedEvent(
        @JsonIgnore UUID eventId,
        @JsonIgnore Instant occurredAt,
        @JsonIgnore String eventType,
        UUID userId,
        UUID decidedBy,
        Instant decidedAt
) implements DomainEvent {

    public static final String TYPE = "moderation.user_reinstated";

    public static UserReinstatedEvent of(UUID userId, UUID decidedBy, Instant now) {
        return new UserReinstatedEvent(UUID.randomUUID(), now, TYPE, userId, decidedBy, now);
    }
}
