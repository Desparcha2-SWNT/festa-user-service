package com.festa.user.api.dto.response;

import java.time.Instant;
import java.util.Set;

public record InterestsResponse(
        Set<String> interests,
        Instant updatedAt
) {
}