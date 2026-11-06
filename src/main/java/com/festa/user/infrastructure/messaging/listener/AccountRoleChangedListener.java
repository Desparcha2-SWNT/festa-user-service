package com.festa.user.infrastructure.messaging.listener;

import com.festa.user.application.handler.AccountRoleChangedHandler;
import com.festa.user.domain.event.inbound.AccountRoleChangedEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AccountRoleChangedListener {

    private static final Logger log = LoggerFactory.getLogger(AccountRoleChangedListener.class);

    private final AccountRoleChangedHandler handler;
    private final Counter consumed;

    public AccountRoleChangedListener(AccountRoleChangedHandler handler, MeterRegistry registry) {
        this.handler = handler;
        this.consumed = Counter.builder("festa_messages_consumed_total")
                .tag("type", "account.role_changed")
                .register(registry);
    }

    @RabbitListener(queues = "${festa.user.rabbit.inbox-queue}")
    public void onMessage(AccountRoleChangedEvent event) {
        try {
            handler.handle(event);
            consumed.increment();
        } catch (Exception ex) {
            log.error("Fallo procesando account.role_changed {}", event.eventId(), ex);
            consumed.increment();
            throw ex;
        }
    }
}