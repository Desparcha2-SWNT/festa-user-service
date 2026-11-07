package com.festa.user.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.festa.user.domain.event.DomainEvent;
import com.festa.user.domain.event.inbound.AccountDeletedEvent;
import com.festa.user.domain.event.inbound.AccountRegisteredEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Lee el sobre del contrato y lo convierte en el evento de dominio. Cualquier dato
 * faltante o inválido, un eventType que user-service no consume o un productor que no es
 * el dueño del dominio es un error permanente: el listener lo manda a la DLQ.
 */
@Component
public class InboundEnvelopeParser {

    /** Lo que user-service consume según contracts/README.md, con su productor dueño. */
    static final Map<String, String> CONSUMED = Map.of(
            "account.registered", "auth-service",
            "account.deleted", "auth-service"
    );

    private final ObjectMapper objectMapper;

    public InboundEnvelopeParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public DomainEvent parse(byte[] body) {
        JsonNode root;
        try {
            root = objectMapper.readTree(body);
        } catch (Exception ex) {
            throw new InvalidMessageException("El mensaje no es JSON", ex);
        }
        if (root == null || !root.isObject()) {
            throw new InvalidMessageException("El mensaje no es un sobre JSON");
        }

        String eventType = text(root, "eventType");
        String owner = CONSUMED.get(eventType);
        if (owner == null) {
            throw new InvalidMessageException("user-service no consume " + eventType);
        }
        String producer = text(root, "producer");
        if (!owner.equals(producer)) {
            throw new InvalidMessageException(eventType + " lo publica " + owner + ", no " + producer);
        }

        UUID eventId = uuid(root, "eventId");
        Instant occurredAt = instant(root, "occurredAt");
        JsonNode data = root.get("data");
        if (data == null || !data.isObject()) {
            throw new InvalidMessageException("Falta data en " + eventType);
        }

        return switch (eventType) {
            case "account.registered" -> new AccountRegisteredEvent(
                    eventId, occurredAt, eventType,
                    uuid(data, "userId"), text(data, "displayName"), text(data, "role"),
                    instant(data, "registeredAt"));
            case "account.deleted" -> new AccountDeletedEvent(
                    eventId, occurredAt, eventType, uuid(data, "userId"));
            default -> throw new InvalidMessageException("user-service no consume " + eventType);
        };
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || !value.isTextual() || value.asText().isBlank()) {
            throw new InvalidMessageException("Falta o no es texto: " + field);
        }
        return value.asText();
    }

    private static UUID uuid(JsonNode node, String field) {
        try {
            return UUID.fromString(text(node, field));
        } catch (IllegalArgumentException ex) {
            throw new InvalidMessageException("No es un UUID: " + field, ex);
        }
    }

    private static Instant instant(JsonNode node, String field) {
        try {
            return Instant.parse(text(node, field));
        } catch (RuntimeException ex) {
            throw new InvalidMessageException("No es una fecha ISO-8601: " + field, ex);
        }
    }

    public static class InvalidMessageException extends RuntimeException {
        public InvalidMessageException(String message) {
            super(message);
        }

        public InvalidMessageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
