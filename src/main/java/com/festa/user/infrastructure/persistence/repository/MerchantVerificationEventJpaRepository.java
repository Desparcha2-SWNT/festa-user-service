package com.festa.user.infrastructure.persistence.repository;

import com.festa.user.infrastructure.persistence.entity.MerchantVerificationEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface MerchantVerificationEventJpaRepository
        extends JpaRepository<MerchantVerificationEventEntity, UUID> {

    @Modifying
    @Query("DELETE FROM MerchantVerificationEventEntity e WHERE e.accountId = :accountId")
    void deleteAllByAccountId(@Param("accountId") UUID accountId);
}