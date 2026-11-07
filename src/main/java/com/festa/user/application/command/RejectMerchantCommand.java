package com.festa.user.application.command;

import com.festa.user.domain.vo.AccountId;

public record RejectMerchantCommand(
        AccountId targetAccountId,
        String reason,
        AccountId actorId,
        String adminBearerToken
) {
}