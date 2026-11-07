package com.festa.user.infrastructure.persistence.mapper;

import com.festa.user.domain.model.UserPreferences;
import com.festa.user.domain.vo.AccountId;
import com.festa.user.domain.vo.BudgetRange;
import com.festa.user.infrastructure.persistence.entity.UserPreferencesEntity;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PreferencesMapper {

    public UserPreferencesEntity toEntity(UserPreferences p) {
        UserPreferencesEntity e = new UserPreferencesEntity();
        e.setAccountId(p.accountId().value());
        e.setBudgetRange(p.budgetRange().name());
        e.setMaxDistanceKm(p.maxDistanceKm());
        e.setUpdatedAt(p.updatedAt());
        e.getPreferredZones().clear();
        e.getPreferredZones().addAll(p.preferredZones());
        e.getPreferredDays().clear();
        e.getPreferredDays().addAll(
                p.preferredDays().stream().map(Enum::name).collect(Collectors.toSet())
        );
        return e;
    }

    public UserPreferences toDomain(UserPreferencesEntity e) {
        Set<String> zones = Set.copyOf(e.getPreferredZones());
        Set<DayOfWeek> days = e.getPreferredDays().stream()
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toUnmodifiableSet());
        return new UserPreferences(
                AccountId.of(e.getAccountId()),
                BudgetRange.valueOf(e.getBudgetRange()),
                zones,
                days,
                e.getMaxDistanceKm(),
                e.getUpdatedAt()
        );
    }
}