package com.festa.user.domain.event.outbound;

import com.festa.user.domain.event.DomainEvent;
import com.festa.user.domain.vo.Interest;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Snapshot de intereses. Lo consume el AI Service (RF8).
 */
public record UserInterestsUpdatedEvent(
        UUID eventId,
        Instant occurredAt,
        String eventType,
        UUID accountId,
        Set<Interest> interests
) implements DomainEvent {

    public static final String TYPE = "user.interests_updated";

    public static UserInterestsUpdatedEvent of(UUID accountId, Set<Interest> interests, Instant now) {
        return new UserInterestsUpdatedEvent(
                UUID.randomUUID(), now, TYPE, accountId,
                interests == null ? Set.of() : Set.copyOf(interests)
        );
    }
}