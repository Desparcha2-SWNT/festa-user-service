package com.festa.user.infrastructure.persistence.repository;

import com.festa.user.infrastructure.persistence.entity.UserInterestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserInterestJpaRepository extends JpaRepository<UserInterestEntity, UserInterestEntity.PK> {

    List<UserInterestEntity> findByAccountId(UUID accountId);

    @Modifying
    @Query("DELETE FROM UserInterestEntity e WHERE e.accountId = :accountId")
    void deleteAllByAccountId(@Param("accountId") UUID accountId);
}