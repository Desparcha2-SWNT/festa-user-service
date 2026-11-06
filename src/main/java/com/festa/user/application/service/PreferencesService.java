package com.festa.user.application.service;

import com.festa.user.application.cache.ProfileCacheService;
import com.festa.user.application.command.UpdatePreferencesCommand;
import com.festa.user.domain.event.outbound.UserPreferencesUpdatedEvent;
import com.festa.user.domain.exception.NotFoundException;
import com.festa.user.domain.model.UserPreferences;
import com.festa.user.domain.port.in.GetPreferencesUseCase;
import com.festa.user.domain.port.in.UpdatePreferencesUseCase;
import com.festa.user.domain.port.out.ClockPort;
import com.festa.user.domain.port.out.OutboxRepositoryPort;
import com.festa.user.domain.port.out.PreferencesRepositoryPort;
import com.festa.user.domain.port.out.ProfileRepositoryPort;
import com.festa.user.domain.vo.AccountId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PreferencesService implements GetPreferencesUseCase, UpdatePreferencesUseCase {

    private final PreferencesRepositoryPort preferences;
    private final ProfileRepositoryPort profiles;
    private final OutboxRepositoryPort outbox;
    private final ClockPort clock;
    private final ProfileCacheService cache;

    public PreferencesService(
            PreferencesRepositoryPort preferences,
            ProfileRepositoryPort profiles,
            OutboxRepositoryPort outbox,
            ClockPort clock,
            ProfileCacheService cache
    ) {
        this.preferences = preferences;
        this.profiles = profiles;
        this.outbox = outbox;
        this.clock = clock;
        this.cache = cache;
    }

    @Override
    @Transactional(readOnly = true)
    public UserPreferences handle(AccountId accountId) {
        profiles.findByAccountId(accountId)
                .orElseThrow(() -> NotFoundException.of("Perfil", accountId));
        return preferences.findByAccountId(accountId)
                .orElseGet(() -> UserPreferences.defaults(accountId, clock.now()));
    }

    @Override
    @Transactional
    public UserPreferences handle(UpdatePreferencesCommand command) {
        profiles.findByAccountId(command.accountId())
                .orElseThrow(() -> NotFoundException.of("Perfil", command.accountId()));

        UserPreferences current = preferences.findByAccountId(command.accountId())
                .orElseGet(() -> UserPreferences.defaults(command.accountId(), clock.now()));

        UserPreferences updated = current.applyUpdate(
                command.budgetRange(),
                command.preferredZones(),
                command.preferredDays(),
                command.maxDistanceKm(),
                clock.now()
        );
        UserPreferences saved = preferences.save(updated);

        outbox.append(UserPreferencesUpdatedEvent.of(
                command.accountId().value(),
                saved.budgetRange(),
                saved.preferredZones(),
                saved.preferredDays(),
                saved.maxDistanceKm(),
                clock.now()
        ));
        cache.evictAllForAccount(command.accountId());

        return saved;
    }
}