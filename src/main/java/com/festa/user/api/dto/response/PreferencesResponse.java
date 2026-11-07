package com.festa.user.api.dto.response;

import java.time.DayOfWeek;
import java.time.Instant;
import java.util.Set;

public record PreferencesResponse(
        String budgetRange,
        Set<String> preferredZones,
        Set<DayOfWeek> preferredDays,
        Integer maxDistanceKm,
        Instant updatedAt
) {
}