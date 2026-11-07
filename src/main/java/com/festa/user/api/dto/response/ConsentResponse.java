package com.festa.user.api.dto.response;

import java.time.Instant;

public record ConsentResponse(
        String type,
        boolean granted,
        String version,
        Instant grantedAt
) {
}