package com.festa.user.domain.model;

import com.festa.user.domain.vo.AccountId;
import com.festa.user.domain.vo.BudgetRange;

import java.time.DayOfWeek;
import java.time.Instant;
import java.util.Set;

/**
 * Preferencias de plan (RF2). Alimentan al AI Service (RF8) para las
 * recomendaciones personalizadas.
 *
 * preferredZones es libre por ahora (el piloto está en ECI, con sub-zonas
 * aún no formalizadas en un enum). Se valida en la capa API con un catálogo
 * de strings permitidos.
 */
public record UserPreferences(
        AccountId accountId,
        BudgetRange budgetRange,
        Set<String> preferredZones,
        Set<DayOfWeek> preferredDays,
        Integer maxDistanceKm,
        Instant updatedAt
) {

    public UserPreferences {
        if (accountId == null) {
            throw new IllegalArgumentException("accountId es obligatorio");
        }
        budgetRange = budgetRange == null ? BudgetRange.ANY : budgetRange;
        preferredZones = preferredZones == null ? Set.of() : Set.copyOf(preferredZones);
        preferredDays = preferredDays == null ? Set.of() : Set.copyOf(preferredDays);
        if (maxDistanceKm != null && (maxDistanceKm < 0 || maxDistanceKm > 100)) {
            throw new IllegalArgumentException("maxDistanceKm debe estar en [0, 100]");
        }
        if (updatedAt == null) {
            throw new IllegalArgumentException("updatedAt es obligatorio");
        }
    }

    public static UserPreferences defaults(AccountId accountId, Instant now) {
        return new UserPreferences(accountId, BudgetRange.ANY, Set.of(), Set.of(), null, now);
    }

    public UserPreferences applyUpdate(
            BudgetRange newBudgetRange,
            Set<String> newPreferredZones,
            Set<DayOfWeek> newPreferredDays,
            Integer newMaxDistanceKm,
            Instant now
    ) {
        return new UserPreferences(
                accountId, newBudgetRange, newPreferredZones, newPreferredDays, newMaxDistanceKm, now
        );
    }
}