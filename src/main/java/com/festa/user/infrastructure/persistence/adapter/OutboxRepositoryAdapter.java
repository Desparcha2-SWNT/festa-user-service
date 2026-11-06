package com.festa.user.infrastructure.persistence.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.festa.user.domain.event.DomainEvent;
import com.festa.user.domain.port.out.OutboxRepositoryPort;
import com.festa.user.infrastructure.persistence.entity.OutboxEntity;
import com.festa.user.infrastructure.persistence.repository.OutboxJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class OutboxRepositoryAdapter implements OutboxRepositoryPort {

    private final OutboxJpaRepository repo;
    private final ObjectMapper objectMapper;

    public OutboxRepositoryAdapter(OutboxJpaRepository repo, ObjectMapper objectMapper) {
        this.repo = repo;
        this.objectMapper = objectMapper;
    }

    @Override
    public void append(DomainEvent event) {
        OutboxEntity e = new OutboxEntity();
        e.setId(event.eventId());
        e.setEventType(event.eventType());
        e.setPayloadJson(serialize(event));
        repo.save(e);
    }

    @Override
    public List<PendingEvent> findPending(int limit) {
        return repo.findPending(PageRequest.of(0, limit)).stream()
                .map(o -> new PendingEvent(o.getId(), o.getEventType(), o.getPayloadJson()))
                .toList();
    }

    @Override
    public void markPublished(UUID id) {
        repo.markPublished(id, Instant.now());
    }

    private String serialize(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(
                    "No se pudo serializar el evento " + event.eventType(), ex
            );
        }
    }
}