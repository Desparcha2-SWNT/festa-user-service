package com.festa.user.api.dto.request;

import com.festa.user.domain.vo.BusinessCategory;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateMerchantRequest(
        @NotBlank @Size(max = 120) String businessName,
        @NotNull BusinessCategory category,
        @NotBlank @Size(max = 300) String address,
        @DecimalMin("-90.0") @DecimalMax("90.0") double latitude,
        @DecimalMin("-180.0") @DecimalMax("180.0") double longitude,
        @Size(max = 2000) String description
) {
}