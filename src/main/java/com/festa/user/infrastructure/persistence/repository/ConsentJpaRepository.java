package com.festa.user.infrastructure.persistence.repository;

import com.festa.user.infrastructure.persistence.entity.ConsentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ConsentJpaRepository extends JpaRepository<ConsentEntity, UUID> {

    /**
     * Devuelve el último consentimiento por tipo. DISTINCT ON es Postgres-específico,
     * por eso nativeQuery.
     */
    @Query(value = """
            SELECT DISTINCT ON (type) *
            FROM consents
            WHERE account_id = :accountId
            ORDER BY type, granted_at DESC
            """, nativeQuery = true)
    List<ConsentEntity> findLatestPerType(@Param("accountId") UUID accountId);

    @Modifying
    @Query("DELETE FROM ConsentEntity e WHERE e.accountId = :accountId")
    void deleteAllByAccountId(@Param("accountId") UUID accountId);
}