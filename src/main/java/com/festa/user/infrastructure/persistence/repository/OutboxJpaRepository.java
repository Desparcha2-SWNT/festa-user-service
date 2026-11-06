package com.festa.user.infrastructure.persistence.repository;

import com.festa.user.infrastructure.persistence.entity.OutboxEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxJpaRepository extends JpaRepository<OutboxEntity, UUID> {

    @Query("""
            SELECT o FROM OutboxEntity o
            WHERE o.publishedAt IS NULL
            ORDER BY o.createdAt ASC
            """)
    List<OutboxEntity> findPending(Pageable pageable);

    @Modifying
    @Query("UPDATE OutboxEntity o SET o.publishedAt = :when WHERE o.id = :id")
    void markPublished(@Param("id") UUID id, @Param("when") Instant when);
}