package com.festa.user.domain.port.out;

import com.festa.user.domain.model.UserInterests;
import com.festa.user.domain.vo.AccountId;

import java.util.Optional;

public interface InterestRepositoryPort {
    UserInterests save(UserInterests interests);
    Optional<UserInterests> findByAccountId(AccountId accountId);
    void deleteByAccountId(AccountId accountId);
}