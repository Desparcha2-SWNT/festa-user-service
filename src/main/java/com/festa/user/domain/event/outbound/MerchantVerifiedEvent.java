package com.festa.user.domain.event.outbound;

import com.festa.user.domain.event.DomainEvent;
import com.festa.user.domain.vo.BusinessCategory;

import java.time.Instant;
import java.util.UUID;

/**
 * Se publica al aprobar un comercio (RF3). Lo consume Event Service para
 * habilitar la creación de eventos por parte de este comercio, y Analytics
 * para RN4/RN5.
 */
public record MerchantVerifiedEvent(
        UUID eventId,
        Instant occurredAt,
        String eventType,
        UUID accountId,
        String businessName,
        BusinessCategory category,
        UUID approvedBy,
        Instant approvedAt
) implements DomainEvent {

    public static final String TYPE = "merchant.verified";

    public static MerchantVerifiedEvent of(
            UUID accountId,
            String businessName,
            BusinessCategory category,
            UUID approvedBy,
            Instant approvedAt,
            Instant now
    ) {
        return new MerchantVerifiedEvent(
                UUID.randomUUID(), now, TYPE, accountId, businessName, category, approvedBy, approvedAt
        );
    }
}