package com.festa.user.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectMerchantRequest(
        @NotBlank @Size(max = 500) String reason
) {
}