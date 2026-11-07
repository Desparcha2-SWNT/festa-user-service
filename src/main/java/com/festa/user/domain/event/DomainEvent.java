package com.festa.user.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Interfaz común para todos los eventos de dominio (in y out).
 * En los eventos de salida, los demás componentes del record son el `data` del
 * contrato (festa-infra/contracts/events/user) y estos tres van al sobre.
 */
public interface DomainEvent {

    UUID eventId();

    Instant occurredAt();

    String eventType();
}
