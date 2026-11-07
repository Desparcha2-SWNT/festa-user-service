package com.festa.user.api.dto.request;

import com.festa.user.domain.vo.ConsentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RecordConsentRequest(
        @NotNull ConsentType type,
        boolean granted,
        // El contrato location_consent.granted limita policyVersion a 20 caracteres.
        @NotBlank @Size(max = 20) String version
) {
}