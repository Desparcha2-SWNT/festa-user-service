package com.festa.user.domain.port.out;

import com.festa.user.domain.model.Profile;
import com.festa.user.domain.vo.AccountId;

import java.util.Optional;

public interface ProfileRepositoryPort {
    Profile save(Profile profile);
    Optional<Profile> findByAccountId(AccountId accountId);
    void deleteByAccountId(AccountId accountId);
}