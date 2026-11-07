package com.festa.user.domain.port.out;

import com.festa.user.domain.vo.AccountId;

/**
 * Cambia el rol de una cuenta llamando al PATCH /auth/admin/accounts/{id}/role.
 * Reenvía el bearer del admin actuante (decisión 2 del lote 1).
 */
public interface AuthAdminClientPort {
    void changeRole(AccountId accountId, String newRole, String adminBearerToken);
}