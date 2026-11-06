package com.festa.user.application.command;

import com.festa.user.domain.vo.AccountId;

/**
 * adminBearerToken: el token del admin actuante. El AuthAdminClient lo reenvía
 * al PATCH /auth/admin/accounts/{id}/role. Decisión 2 del lote 1.
 */
public record ApproveMerchantCommand(
        AccountId targetAccountId,
        AccountId actorId,
        String adminBearerToken
) {
}