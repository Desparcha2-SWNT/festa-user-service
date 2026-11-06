package com.festa.user.api.dto.request;

import com.festa.user.domain.vo.ConsentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RecordConsentRequest(
        @NotNull ConsentType type,
        boolean granted,
        @NotBlank @Size(max = 32) String version
) {
}