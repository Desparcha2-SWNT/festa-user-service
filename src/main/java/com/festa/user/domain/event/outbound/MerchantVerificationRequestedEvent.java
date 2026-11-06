package com.festa.user.domain.event.outbound;

import com.festa.user.domain.event.DomainEvent;
import com.festa.user.domain.vo.BusinessCategory;

import java.time.Instant;
import java.util.UUID;

/**
 * Se publica cuando un usuario envía (o reenvía) un perfil de comercio.
 * Analytics lo usa para medir la cola de verificación manual (RN4).
 */
public record MerchantVerificationRequestedEvent(
        UUID eventId,
        Instant occurredAt,
        String eventType,
        UUID accountId,
        String businessName,
        BusinessCategory category,
        Instant submittedAt
) implements DomainEvent {

    public static final String TYPE = "merchant.verification_requested";

    public static MerchantVerificationRequestedEvent of(
            UUID accountId,
            String businessName,
            BusinessCategory category,
            Instant submittedAt,
            Instant now
    ) {
        return new MerchantVerificationRequestedEvent(
                UUID.randomUUID(), now, TYPE, accountId, businessName, category, submittedAt
        );
    }
}