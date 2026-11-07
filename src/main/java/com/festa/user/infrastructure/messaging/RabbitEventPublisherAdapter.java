package com.festa.user.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.festa.user.config.RabbitConfig;
import com.festa.user.domain.port.out.EventPublisherPort;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Publica el sobre tal cual quedó en la outbox, con las propiedades AMQP del contrato,
 * y espera el publisher confirm: si no llega, lanza y el evento sigue pendiente en la outbox.
 */
@Component
public class RabbitEventPublisherAdapter implements EventPublisherPort {

    static final String CORRELATION_HEADER = "x-correlation-id";
    private static final long CONFIRM_TIMEOUT_SECONDS = 10;

    private final RabbitTemplate rabbit;
    private final ObjectMapper objectMapper;

    public RabbitEventPublisherAdapter(RabbitTemplate rabbit, ObjectMapper objectMapper) {
        this.rabbit = rabbit;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(String eventType, String payloadJson, String messageId) {
        MessageProperties props = new MessageProperties();
        props.setMessageId(messageId);
        props.setType(eventType);
        props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        props.setContentEncoding(StandardCharsets.UTF_8.name());
        props.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        props.setHeader(CORRELATION_HEADER, correlationId(payloadJson, messageId));

        Message message = new Message(payloadJson.getBytes(StandardCharsets.UTF_8), props);
        CorrelationData confirm = new CorrelationData(messageId);
        rabbit.send(RabbitConfig.USER_EXCHANGE, eventType, message, confirm);
        awaitConfirm(confirm, eventType);
    }

    static void awaitConfirm(CorrelationData confirm, String what) {
        CorrelationData.Confirm result;
        try {
            result = confirm.getFuture().get(CONFIRM_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrumpido esperando confirmación de " + what, ex);
        } catch (Exception ex) {
            throw new IllegalStateException("Sin confirmación del broker para " + what, ex);
        }
        if (!result.isAck()) {
            throw new IllegalStateException("El broker rechazó " + what + ": " + result.getReason());
        }
        if (confirm.getReturned() != null) {
            throw new IllegalStateException("Sin ruta en el broker para " + what);
        }
    }

    private String correlationId(String payloadJson, String fallback) {
        try {
            String value = objectMapper.readTree(payloadJson).path("correlationId").asText(null);
            return value == null || value.isBlank() ? fallback : value;
        } catch (Exception ex) {
            return fallback;
        }
    }
}
