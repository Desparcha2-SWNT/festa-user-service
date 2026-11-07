package com.festa.user.application.command;

import com.festa.user.domain.vo.AccountId;
import com.festa.user.domain.vo.BusinessCategory;

public record UpdateMerchantCommand(
        AccountId accountId,
        String businessName,
        BusinessCategory category,
        String address,
        double latitude,
        double longitude,
        String description
) {
}