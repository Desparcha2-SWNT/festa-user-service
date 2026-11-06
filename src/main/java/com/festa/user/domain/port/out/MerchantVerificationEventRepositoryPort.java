package com.festa.user.domain.port.out;

import com.festa.user.domain.model.MerchantVerificationEvent;

public interface MerchantVerificationEventRepositoryPort {
    void save(MerchantVerificationEvent event);
    void deleteByAccountId(com.festa.user.domain.vo.AccountId accountId);
}