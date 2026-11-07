package com.festa.user.infrastructure.messaging;

import com.festa.user.application.handler.AccountDeletedHandler;
import com.festa.user.application.handler.AccountRegisteredHandler;
import com.festa.user.config.AppProperties;
import com.festa.user.domain.event.DomainEvent;
import com.festa.user.domain.event.inbound.AccountDeletedEvent;
import com.festa.user.domain.event.inbound.AccountRegisteredEvent;
import com.rabbitmq.client.Channel;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Único consumidor de user-service.inbox (contracts/README.md § Consumir):
 *  - sobre inválido, tipo no consumido o productor falso: nack sin requeue, va a la DLQ;
 *  - error transitorio: se republica en .retry con x-retry-count + 1 y se hace ack;
 *  - desde x-retry-count >= 3: nack sin requeue, va a la DLQ.
 * La idempotencia (processed_events) la hace cada handler en su transacción.
 */
@Component
public class InboxMessageListener {

    static final String RETRY_HEADER = "x-retry-count";
    static final int MAX_RETRIES = 3;

    private static final Logger log = LoggerFactory.getLogger(InboxMessageListener.class);

    private final InboundEnvelopeParser parser;
    private final AccountRegisteredHandler registered;
    private final AccountDeletedHandler deleted;
    private final RabbitTemplate rabbit;
    private final String retryQueue;
    private final MeterRegistry registry;

    public InboxMessageListener(
            InboundEnvelopeParser parser,
            AccountRegisteredHandler registered,
            AccountDeletedHandler deleted,
            RabbitTemplate rabbit,
            AppProperties props,
            MeterRegistry registry
    ) {
        this.parser = parser;
        this.registered = registered;
        this.deleted = deleted;
        this.rabbit = rabbit;
        this.retryQueue = props.rabbit().retryQueue();
        this.registry = registry;
    }

    @RabbitListener(queues = "${festa.user.rabbit.inbox-queue}", ackMode = "MANUAL")
    public void onMessage(Message message, Channel channel) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();

        DomainEvent event;
        try {
            event = parser.parse(message.getBody());
        } catch (InboundEnvelopeParser.InvalidMessageException ex) {
            log.error("Mensaje inválido en el inbox, va a la DLQ: {}", ex.getMessage());
            count("invalid", "dlq");
            channel.basicNack(tag, false, false);
            return;
        }

        try {
            dispatch(event);
            count(event.eventType(), "ok");
            channel.basicAck(tag, false);
        } catch (Exception ex) {
            handleFailure(message, channel, tag, event, ex);
        }
    }

    void dispatch(DomainEvent event) {
        if (event instanceof AccountRegisteredEvent e) {
            registered.handle(e);
        } else if (event instanceof AccountDeletedEvent e) {
            deleted.handle(e);
        } else {
            throw new IllegalStateException("Sin handler para " + event.eventType());
        }
    }

    private void handleFailure(Message message, Channel channel, long tag, DomainEvent event, Exception cause)
            throws IOException {
        int retries = retryCount(message.getMessageProperties());
        if (retries >= MAX_RETRIES) {
            log.error("{} {} agotó {} reintentos, va a la DLQ", event.eventType(), event.eventId(), retries, cause);
            count(event.eventType(), "dlq");
            channel.basicNack(tag, false, false);
            return;
        }
        try {
            MessageProperties props = message.getMessageProperties();
            props.setHeader(RETRY_HEADER, retries + 1);
            CorrelationData confirm = new CorrelationData(event.eventId() + "-retry-" + (retries + 1));
            rabbit.send("", retryQueue, new Message(message.getBody(), props), confirm);
            RabbitEventPublisherAdapter.awaitConfirm(confirm, "reintento de " + event.eventType());
            log.warn("{} {} falló (intento {}), se reintenta: {}",
                    event.eventType(), event.eventId(), retries + 1, cause.getMessage());
            count(event.eventType(), "retry");
            channel.basicAck(tag, false);
        } catch (Exception republishFailed) {
            // Sin confirmación del reintento no se hace ack: el broker lo vuelve a entregar.
            log.error("No se pudo encolar el reintento de {} {}", event.eventType(), event.eventId(), republishFailed);
            channel.basicNack(tag, false, true);
        }
    }

    private static int retryCount(MessageProperties props) {
        Object value = props.getHeaders().get(RETRY_HEADER);
        return value instanceof Number n ? n.intValue() : 0;
    }

    private void count(String type, String outcome) {
        Counter.builder("festa_messages_consumed_total")
                .tag("type", type)
                .tag("outcome", outcome)
                .register(registry)
                .increment();
    }
}
