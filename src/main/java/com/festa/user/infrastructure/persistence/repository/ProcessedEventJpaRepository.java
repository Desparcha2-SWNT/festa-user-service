package com.festa.user.infrastructure.persistence.repository;

import com.festa.user.infrastructure.persistence.entity.ProcessedEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ProcessedEventJpaRepository
        extends JpaRepository<ProcessedEventEntity, ProcessedEventEntity.PK> {

    /**
     * INSERT ... ON CONFLICT DO NOTHING. Devuelve 1 si insertó, 0 si ya existía.
     */
    @Modifying
    @Query(value = """
            INSERT INTO processed_events (event_id, consumer, processed_at)
            VALUES (:eventId, :consumer, NOW())
            ON CONFLICT (event_id, consumer) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(@Param("eventId") UUID eventId, @Param("consumer") String consumer);
}