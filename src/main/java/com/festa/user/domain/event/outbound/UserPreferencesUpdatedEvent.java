package com.festa.user.domain.event.outbound;

import com.festa.user.domain.event.DomainEvent;
import com.festa.user.domain.vo.BudgetRange;

import java.time.DayOfWeek;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Snapshot completo de preferencias. Lo consume el AI Service (RF8)
 * para recomendaciones sin tener que consultar de vuelta.
 */
public record UserPreferencesUpdatedEvent(
        UUID eventId,
        Instant occurredAt,
        String eventType,
        UUID accountId,
        BudgetRange budgetRange,
        Set<String> preferredZones,
        Set<DayOfWeek> preferredDays,
        Integer maxDistanceKm
) implements DomainEvent {

    public static final String TYPE = "user.preferences_updated";

    public static UserPreferencesUpdatedEvent of(
            UUID accountId,
            BudgetRange budgetRange,
            Set<String> preferredZones,
            Set<DayOfWeek> preferredDays,
            Integer maxDistanceKm,
            Instant now
    ) {
        return new UserPreferencesUpdatedEvent(
                UUID.randomUUID(), now, TYPE, accountId, budgetRange,
                preferredZones == null ? Set.of() : Set.copyOf(preferredZones),
                preferredDays == null ? Set.of() : Set.copyOf(preferredDays),
                maxDistanceKm
        );
    }
}