package com.festa.user.application.worker;

import com.festa.user.config.AppProperties;
import com.festa.user.domain.port.out.EventPublisherPort;
import com.festa.user.domain.port.out.OutboxRepositoryPort;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Drena la tabla outbox y publica al broker. Corre en un solo hilo
 * (piloto, no competimos por batches). runOnce() es invocable desde tests.
 */
@Component
public class OutboxPublisherWorker {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisherWorker.class);

    private final OutboxRepositoryPort outbox;
    private final EventPublisherPort publisher;
    private final AppProperties props;
    private final TransactionTemplate tx;
    private final ScheduledExecutorService scheduler;
    private final Counter publishedCounter;
    private volatile int lastPendingCount = 0;

    public OutboxPublisherWorker(
            OutboxRepositoryPort outbox,
            EventPublisherPort publisher,
            AppProperties props,
            PlatformTransactionManager txManager,
            MeterRegistry meterRegistry
    ) {
        this.outbox = outbox;
        this.publisher = publisher;
        this.props = props;
        this.tx = new TransactionTemplate(txManager);
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "outbox-publisher");
            t.setDaemon(true);
            return t;
        });
        this.publishedCounter = Counter.builder("festa_outbox_published_total")
                .description("Eventos publicados desde el outbox")
                .register(meterRegistry);
        Gauge.builder("festa_outbox_pending", this, OutboxPublisherWorker::lastPendingCount)
                .description("Eventos pendientes en el outbox")
                .register(meterRegistry);
    }

    @PostConstruct
    void start() {
        long periodMs = props.outbox().pollInterval().toMillis();
        scheduler.scheduleWithFixedDelay(this::safeRun, periodMs, periodMs, TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    void stop() throws InterruptedException {
        scheduler.shutdown();
        if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
            scheduler.shutdownNow();
        }
    }

    public void runOnce() {
        List<OutboxRepositoryPort.PendingEvent> pending =
                outbox.findPending(props.outbox().batchSize());
        lastPendingCount = pending.size();

        for (OutboxRepositoryPort.PendingEvent event : pending) {
            try {
                tx.executeWithoutResult(status -> {
                    publisher.publish(event.eventType(), event.payloadJson(), event.id().toString());
                    outbox.markPublished(event.id());
                });
                publishedCounter.increment();
            } catch (Exception ex) {
                log.warn("Fallo publicando outbox {} ({}): {}",
                        event.id(), event.eventType(), ex.getMessage());
            }
        }
    }

    public int lastPendingCount() {
        return lastPendingCount;
    }

    private void safeRun() {
        try {
            runOnce();
        } catch (Exception ex) {
            log.error("Outbox worker cycle falló", ex);
        }
    }
}