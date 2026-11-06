package com.festa.user.domain.port.out;

import com.festa.user.domain.model.MerchantProfile;
import com.festa.user.domain.vo.AccountId;

import java.util.List;
import java.util.Optional;

public interface MerchantRepositoryPort {
    MerchantProfile save(MerchantProfile merchant);
    Optional<MerchantProfile> findByAccountId(AccountId accountId);
    List<MerchantProfile> findByStatus(String status); // "PENDING" para admin
    void deleteByAccountId(AccountId accountId);
}