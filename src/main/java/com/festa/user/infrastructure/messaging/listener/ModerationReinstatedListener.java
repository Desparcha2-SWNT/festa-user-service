package com.festa.user.infrastructure.messaging.listener;

import com.festa.user.application.handler.ModerationReinstatedHandler;
import com.festa.user.domain.event.inbound.ModerationUserReinstatedEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ModerationReinstatedListener {

    private static final Logger log = LoggerFactory.getLogger(ModerationReinstatedListener.class);

    private final ModerationReinstatedHandler handler;
    private final Counter consumed;

    public ModerationReinstatedListener(ModerationReinstatedHandler handler, MeterRegistry registry) {
        this.handler = handler;
        this.consumed = Counter.builder("festa_messages_consumed_total")
                .tag("type", "moderation.user_reinstated")
                .register(registry);
    }

    @RabbitListener(queues = "${festa.user.rabbit.inbox-queue}")
    public void onMessage(ModerationUserReinstatedEvent event) {
        try {
            handler.handle(event);
            consumed.increment();
        } catch (Exception ex) {
            log.error("Fallo procesando moderation.user_reinstated {}", event.eventId(), ex);
            consumed.increment();
            throw ex;
        }
    }
}