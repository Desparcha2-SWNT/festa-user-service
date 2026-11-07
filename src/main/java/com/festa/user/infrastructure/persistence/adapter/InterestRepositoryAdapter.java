package com.festa.user.infrastructure.persistence.adapter;

import com.festa.user.domain.model.UserInterests;
import com.festa.user.domain.port.out.InterestRepositoryPort;
import com.festa.user.domain.vo.AccountId;
import com.festa.user.infrastructure.persistence.entity.UserInterestEntity;
import com.festa.user.infrastructure.persistence.mapper.InterestsMapper;
import com.festa.user.infrastructure.persistence.repository.UserInterestJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class InterestRepositoryAdapter implements InterestRepositoryPort {

    private final UserInterestJpaRepository repo;
    private final InterestsMapper mapper;

    public InterestRepositoryAdapter(UserInterestJpaRepository repo, InterestsMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public UserInterests save(UserInterests interests) {
        repo.deleteAllByAccountId(interests.accountId().value());
        List<UserInterestEntity> entities = mapper.toEntities(interests.accountId(), interests.interests());
        if (!entities.isEmpty()) {
            repo.saveAll(entities);
        }
        return interests;
    }

    @Override
    public Optional<UserInterests> findByAccountId(AccountId accountId) {
        List<UserInterestEntity> rows = repo.findByAccountId(accountId.value());
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(mapper.toDomain(accountId, rows, Instant.now()));
    }

    @Override
    public void deleteByAccountId(AccountId accountId) {
        repo.deleteAllByAccountId(accountId.value());
    }
}