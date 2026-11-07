package com.festa.user.domain.event.outbound;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.festa.user.domain.event.DomainEvent;
import com.festa.user.domain.vo.Interest;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Contrato profile.updated: snapshot de nombre e intereses (lo consumen ai y notification). */
public record ProfileUpdatedEvent(
        @JsonIgnore UUID eventId,
        @JsonIgnore Instant occurredAt,
        @JsonIgnore String eventType,
        UUID userId,
        String displayName,
        List<String> interests,
        Instant updatedAt
) implements DomainEvent {

    public static final String TYPE = "profile.updated";

    public static ProfileUpdatedEvent of(UUID userId, String displayName, Set<Interest> interests, Instant now) {
        List<String> names = interests == null ? List.of()
                : interests.stream().map(i -> i.name().toLowerCase()).sorted().toList();
        return new ProfileUpdatedEvent(UUID.randomUUID(), now, TYPE, userId, displayName, names, now);
    }
}
