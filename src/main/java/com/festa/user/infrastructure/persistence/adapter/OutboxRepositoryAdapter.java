package com.festa.user.infrastructure.persistence.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.festa.user.api.filter.RequestIdFilter;
import com.festa.user.domain.event.DomainEvent;
import com.festa.user.domain.port.out.OutboxRepositoryPort;
import com.festa.user.infrastructure.persistence.entity.OutboxEntity;
import com.festa.user.infrastructure.persistence.repository.OutboxJpaRepository;
import org.slf4j.MDC;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class OutboxRepositoryAdapter implements OutboxRepositoryPort {

    public static final String PRODUCER = "user-service";
    public static final int VERSION = 1;

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
        e.setPayloadJson(serialize(envelope(event, MDC.get(RequestIdFilter.MDC_KEY), null)));
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

    /** Sobre de festa-infra/contracts/envelope.schema.json; los nulos se escriben explícitos. */
    public ObjectNode envelope(DomainEvent event, String correlationId, String causationId) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("eventId", event.eventId().toString());
        node.put("eventType", event.eventType());
        node.put("version", VERSION);
        node.put("occurredAt", event.occurredAt().toString());
        node.put("producer", PRODUCER);
        node.put("correlationId", correlationId);
        node.put("causationId", causationId);
        node.set("data", objectMapper.valueToTree(event));
        return node;
    }

    private String serialize(ObjectNode envelope) {
        try {
            return objectMapper.writeValueAsString(envelope);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(
                    "No se pudo serializar el evento " + envelope.path("eventType").asText(), ex
            );
        }
    }
}
