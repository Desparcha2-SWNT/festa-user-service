package com.festa.user.application.handler;

import com.festa.user.domain.port.out.ProcessedEventsRepositoryPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

/**
 * Envuelve mutación + tryMarkProcessed + outbox en una sola transacción.
 * Si el evento ya fue procesado por este consumer, no ejecuta nada y retorna false.
 */
@Component
public class InboxSupport {

    private final ProcessedEventsRepositoryPort processed;
    private final TransactionTemplate tx;

    public InboxSupport(ProcessedEventsRepositoryPort processed, PlatformTransactionManager txManager) {
        this.processed = processed;
        this.tx = new TransactionTemplate(txManager);
    }

    public boolean runOnce(UUID eventId, String consumer, Runnable work) {
        Boolean applied = tx.execute(status -> {
            if (!processed.tryMarkProcessed(eventId, consumer)) {
                return false;
            }
            work.run();
            return true;
        });
        return Boolean.TRUE.equals(applied);
    }
}