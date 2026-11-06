package com.festa.user.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Interfaz común para todos los eventos de dominio (in y out).
 * Se usa para uniformar el contrato de eventos (eventId para idempotencia,
 * occurredAt para auditoría/orden, eventType para routing y observabilidad).
 */
public interface DomainEvent {

    UUID eventId();

    Instant occurredAt();

    String eventType();
}