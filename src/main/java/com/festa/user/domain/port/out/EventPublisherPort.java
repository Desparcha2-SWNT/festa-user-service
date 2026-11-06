package com.festa.user.domain.port.out;

/**
 * Publica al broker un mensaje ya serializado. Es invocado por el
 * OutboxPublisherWorker, no por los casos de uso.
 */
public interface EventPublisherPort {
    void publish(String eventType, String payloadJson, String messageId);
}