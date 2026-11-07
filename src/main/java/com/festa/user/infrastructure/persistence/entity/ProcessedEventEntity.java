package com.festa.user.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "processed_events")
@IdClass(ProcessedEventEntity.PK.class)
public class ProcessedEventEntity {

    @Id
    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Id
    @Column(name = "consumer", nullable = false, updatable = false, length = 64)
    private String consumer;

    @Column(name = "processed_at", nullable = false, insertable = false, updatable = false)
    private Instant processedAt;

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }
    public String getConsumer() { return consumer; }
    public void setConsumer(String consumer) { this.consumer = consumer; }
    public Instant getProcessedAt() { return processedAt; }

    public static class PK implements Serializable {
        private UUID eventId;
        private String consumer;

        public PK() {}
        public PK(UUID eventId, String consumer) {
            this.eventId = eventId;
            this.consumer = consumer;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PK pk)) return false;
            return Objects.equals(eventId, pk.eventId) && Objects.equals(consumer, pk.consumer);
        }

        @Override
        public int hashCode() { return Objects.hash(eventId, consumer); }
    }
}