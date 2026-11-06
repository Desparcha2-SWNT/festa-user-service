package com.festa.user.infrastructure.persistence.adapter;

import com.festa.user.domain.model.UserPreferences;
import com.festa.user.domain.port.out.PreferencesRepositoryPort;
import com.festa.user.domain.vo.AccountId;
import com.festa.user.infrastructure.persistence.mapper.PreferencesMapper;
import com.festa.user.infrastructure.persistence.repository.UserPreferencesJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PreferencesRepositoryAdapter implements PreferencesRepositoryPort {

    private final UserPreferencesJpaRepository repo;
    private final PreferencesMapper mapper;

    public PreferencesRepositoryAdapter(UserPreferencesJpaRepository repo, PreferencesMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public UserPreferences save(UserPreferences preferences) {
        return mapper.toDomain(repo.save(mapper.toEntity(preferences)));
    }

    @Override
    public Optional<UserPreferences> findByAccountId(AccountId accountId) {
        return repo.findById(accountId.value()).map(mapper::toDomain);
    }

    @Override
    public void deleteByAccountId(AccountId accountId) {
        repo.deleteById(accountId.value());
    }
}