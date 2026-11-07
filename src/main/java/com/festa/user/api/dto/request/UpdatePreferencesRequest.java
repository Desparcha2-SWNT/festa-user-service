package com.festa.user.api.dto.request;

import com.festa.user.domain.vo.BudgetRange;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.DayOfWeek;
import java.util.Set;

public record UpdatePreferencesRequest(
        BudgetRange budgetRange,
        @Size(max = 20) Set<@Size(min = 1, max = 64) String> preferredZones,
        Set<DayOfWeek> preferredDays,
        @Min(0) @Max(100) Integer maxDistanceKm
) {
}