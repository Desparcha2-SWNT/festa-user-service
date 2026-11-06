package com.festa.user.api.dto.response;

import java.time.Instant;
import java.util.UUID;

public record MerchantResponse(
        UUID accountId,
        String businessName,
        String category,
        String address,
        double latitude,
        double longitude,
        String description,
        String verificationStatus,
        boolean suspended,
        String rejectionReason,
        Instant createdAt,
        Instant updatedAt
) {
}