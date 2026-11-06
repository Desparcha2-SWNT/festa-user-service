package com.festa.user.api.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProfileResponse(
        UUID accountId,
        String bio,
        String avatarUrl,
        String phone,
        Instant createdAt,
        Instant updatedAt,
        List<String> interests,
        PreferencesResponse preferences,
        List<ConsentResponse> consents,
        MerchantResponse merchant
) {
}