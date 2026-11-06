package com.festa.user.infrastructure.persistence.adapter;

import com.festa.user.domain.model.Profile;
import com.festa.user.domain.port.out.ProfileRepositoryPort;
import com.festa.user.domain.vo.AccountId;
import com.festa.user.infrastructure.persistence.mapper.ProfileMapper;
import com.festa.user.infrastructure.persistence.repository.ProfileJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ProfileRepositoryAdapter implements ProfileRepositoryPort {

    private final ProfileJpaRepository repo;
    private final ProfileMapper mapper;

    public ProfileRepositoryAdapter(ProfileJpaRepository repo, ProfileMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public Profile save(Profile profile) {
        return mapper.toDomain(repo.save(mapper.toEntity(profile)));
    }

    @Override
    public Optional<Profile> findByAccountId(AccountId accountId) {
        return repo.findById(accountId.value()).map(mapper::toDomain);
    }

    @Override
    public void deleteByAccountId(AccountId accountId) {
        repo.deleteById(accountId.value());
    }
}