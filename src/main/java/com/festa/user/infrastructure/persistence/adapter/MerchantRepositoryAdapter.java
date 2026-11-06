package com.festa.user.infrastructure.persistence.adapter;

import com.festa.user.domain.model.MerchantProfile;
import com.festa.user.domain.port.out.MerchantRepositoryPort;
import com.festa.user.domain.vo.AccountId;
import com.festa.user.infrastructure.persistence.mapper.MerchantMapper;
import com.festa.user.infrastructure.persistence.repository.MerchantJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class MerchantRepositoryAdapter implements MerchantRepositoryPort {

    private final MerchantJpaRepository repo;
    private final MerchantMapper mapper;

    public MerchantRepositoryAdapter(MerchantJpaRepository repo, MerchantMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public MerchantProfile save(MerchantProfile merchant) {
        return mapper.toDomain(repo.save(mapper.toEntity(merchant)));
    }

    @Override
    public Optional<MerchantProfile> findByAccountId(AccountId accountId) {
        return repo.findById(accountId.value()).map(mapper::toDomain);
    }

    @Override
    public List<MerchantProfile> findByStatus(String status) {
        return repo.findByVerificationStatusOrderByUpdatedAtAsc(status).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void deleteByAccountId(AccountId accountId) {
        repo.deleteById(accountId.value());
    }
}