package com.festa.user.infrastructure.messaging.listener;

import com.festa.user.application.handler.AccountDeletedHandler;
import com.festa.user.domain.event.inbound.AccountDeletedEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AccountDeletedListener {

    private static final Logger log = LoggerFactory.getLogger(AccountDeletedListener.class);

    private final AccountDeletedHandler handler;
    private final Counter consumed;

    public AccountDeletedListener(AccountDeletedHandler handler, MeterRegistry registry) {
        this.handler = handler;
        this.consumed = Counter.builder("festa_messages_consumed_total")
                .tag("type", "account.deleted")
                .register(registry);
    }

    @RabbitListener(queues = "${festa.user.rabbit.inbox-queue}")
    public void onMessage(AccountDeletedEvent event) {
        try {
            handler.handle(event);
            consumed.increment();
        } catch (Exception ex) {
            log.error("Fallo procesando account.deleted {}", event.eventId(), ex);
            consumed.increment();
            throw ex;
        }
    }
}