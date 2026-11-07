package com.festa.user.domain.event.outbound;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.festa.user.domain.event.DomainEvent;
import com.festa.user.domain.model.MerchantProfile;
import com.festa.user.domain.vo.MerchantStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Contrato merchant.updated: un comercio que estaba verificado cambió de datos o perdió
 * la verificación. Con verified=false, event-service y payment dejan de tratarlo como verificado.
 */
public record MerchantUpdatedEvent(
        @JsonIgnore UUID eventId,
        @JsonIgnore Instant occurredAt,
        @JsonIgnore String eventType,
        UUID merchantId,
        UUID userId,
        String businessName,
        String category,
        MerchantPlace place,
        boolean verified,
        Instant updatedAt
) implements DomainEvent {

    public static final String TYPE = "merchant.updated";

    public static MerchantUpdatedEvent of(MerchantProfile m, Instant now) {
        UUID id = m.accountId().value();
        return new MerchantUpdatedEvent(
                UUID.randomUUID(), now, TYPE, id, id, m.businessName(),
                m.category().name().toLowerCase(), MerchantPlace.of(m),
                m.verificationStatus() == MerchantStatus.VERIFIED, m.updatedAt()
        );
    }
}
