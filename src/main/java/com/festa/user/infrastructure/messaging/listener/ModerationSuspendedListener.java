package com.festa.user.infrastructure.messaging.listener;

import com.festa.user.application.handler.ModerationSuspendedHandler;
import com.festa.user.domain.event.inbound.ModerationUserSuspendedEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ModerationSuspendedListener {

    private static final Logger log = LoggerFactory.getLogger(ModerationSuspendedListener.class);

    private final ModerationSuspendedHandler handler;
    private final Counter consumed;

    public ModerationSuspendedListener(ModerationSuspendedHandler handler, MeterRegistry registry) {
        this.handler = handler;
        this.consumed = Counter.builder("festa_messages_consumed_total")
                .tag("type", "moderation.user_suspended")
                .register(registry);
    }

    @RabbitListener(queues = "${festa.user.rabbit.inbox-queue}")
    public void onMessage(ModerationUserSuspendedEvent event) {
        try {
            handler.handle(event);
            consumed.increment();
        } catch (Exception ex) {
            log.error("Fallo procesando moderation.user_suspended {}", event.eventId(), ex);
            consumed.increment();
            throw ex;
        }
    }
}