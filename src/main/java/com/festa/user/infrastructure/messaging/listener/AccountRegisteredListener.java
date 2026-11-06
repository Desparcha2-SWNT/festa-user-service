package com.festa.user.infrastructure.messaging.listener;

import com.festa.user.application.handler.AccountRegisteredHandler;
import com.festa.user.domain.event.inbound.AccountRegisteredEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AccountRegisteredListener {

    private static final Logger log = LoggerFactory.getLogger(AccountRegisteredListener.class);

    private final AccountRegisteredHandler handler;
    private final Counter consumed;

    public AccountRegisteredListener(AccountRegisteredHandler handler, MeterRegistry registry) {
        this.handler = handler;
        this.consumed = Counter.builder("festa_messages_consumed_total")
                .tag("type", "account.registered")
                .register(registry);
    }

    @RabbitListener(queues = "${festa.user.rabbit.inbox-queue}")
    public void onMessage(AccountRegisteredEvent event) {
        try {
            handler.handle(event);
            consumed.increment();
        } catch (Exception ex) {
            log.error("Fallo procesando account.registered {}", event.eventId(), ex);
            consumed.increment();
            throw ex; // provoca nack y va a .retry vía DLX
        }
    }
}