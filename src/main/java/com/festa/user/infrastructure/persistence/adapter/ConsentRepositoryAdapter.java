package com.festa.user.infrastructure.persistence.adapter;

import com.festa.user.domain.model.Consent;
import com.festa.user.domain.port.out.ConsentRepositoryPort;
import com.festa.user.domain.vo.AccountId;
import com.festa.user.infrastructure.persistence.mapper.ConsentMapper;
import com.festa.user.infrastructure.persistence.repository.ConsentJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConsentRepositoryAdapter implements ConsentRepositoryPort {

    private final ConsentJpaRepository repo;
    private final ConsentMapper mapper;

    public ConsentRepositoryAdapter(ConsentJpaRepository repo, ConsentMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public Consent save(Consent consent) {
        return mapper.toDomain(repo.save(mapper.toEntity(consent)));
    }

    @Override
    public List<Consent> findLatestPerType(AccountId accountId) {
        return repo.findLatestPerType(accountId.value()).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void deleteByAccountId(AccountId accountId) {
        repo.deleteAllByAccountId(accountId.value());
    }
}