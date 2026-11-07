package com.festa.user.domain.port.out;

import com.festa.user.domain.model.UserPreferences;
import com.festa.user.domain.vo.AccountId;

import java.util.Optional;

public interface PreferencesRepositoryPort {
    UserPreferences save(UserPreferences preferences);
    Optional<UserPreferences> findByAccountId(AccountId accountId);
    void deleteByAccountId(AccountId accountId);
}