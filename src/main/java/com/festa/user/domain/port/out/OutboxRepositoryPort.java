package com.festa.user.domain.port.out;

import com.festa.user.domain.event.DomainEvent;

import java.util.List;
import java.util.UUID;

/**
 * Escritura dentro de la transacción del caso de uso.
 * Lectura para el OutboxPublisherWorker (polling por batch).
 */
public interface OutboxRepositoryPort {

    record PendingEvent(UUID id, String eventType, String payloadJson) {
    }

    void append(DomainEvent event);

    List<PendingEvent> findPending(int limit);

    void markPublished(UUID id);
}