package com.festa.user.application.command;

import com.festa.user.domain.vo.AccountId;
import com.festa.user.domain.vo.BudgetRange;

import java.time.DayOfWeek;
import java.util.Set;

public record UpdatePreferencesCommand(
        AccountId accountId,
        BudgetRange budgetRange,
        Set<String> preferredZones,
        Set<DayOfWeek> preferredDays,
        Integer maxDistanceKm
) {
}