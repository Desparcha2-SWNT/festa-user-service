package com.festa.user.infrastructure.persistence.adapter;

import com.festa.user.domain.model.MerchantVerificationEvent;
import com.festa.user.domain.port.out.MerchantVerificationEventRepositoryPort;
import com.festa.user.domain.vo.AccountId;
import com.festa.user.infrastructure.persistence.mapper.MerchantVerificationEventMapper;
import com.festa.user.infrastructure.persistence.repository.MerchantVerificationEventJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class MerchantVerificationEventRepositoryAdapter
        implements MerchantVerificationEventRepositoryPort {

    private final MerchantVerificationEventJpaRepository repo;
    private final MerchantVerificationEventMapper mapper;

    public MerchantVerificationEventRepositoryAdapter(
            MerchantVerificationEventJpaRepository repo,
            MerchantVerificationEventMapper mapper
    ) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public void save(MerchantVerificationEvent event) {
        repo.save(mapper.toEntity(event));
    }

    @Override
    public void deleteByAccountId(AccountId accountId) {
        repo.deleteAllByAccountId(accountId.value());
    }
}