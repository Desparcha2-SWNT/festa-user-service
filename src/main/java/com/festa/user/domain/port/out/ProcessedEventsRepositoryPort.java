package com.festa.user.domain.port.out;

import java.util.UUID;

/**
 * Idempotencia del inbox: cada handler registra (eventId, consumer) en la misma
 * transacción que la mutación de dominio.
 */
public interface ProcessedEventsRepositoryPort {

    /** true si ya fue procesado por este consumer; false si es la primera vez. */
    boolean tryMarkProcessed(UUID eventId, String consumer);
}