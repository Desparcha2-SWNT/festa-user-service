package com.festa.user.api.dto.response;

import java.util.UUID;

public record PublicMerchantResponse(
        UUID accountId,
        String businessName,
        String category,
        String address,
        double latitude,
        double longitude,
        String description
) {
}