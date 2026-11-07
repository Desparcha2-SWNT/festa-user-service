package com.festa.user.application.query;

import com.festa.user.domain.vo.AccountId;
import com.festa.user.domain.vo.BusinessCategory;

/**
 * Vista pública de un comercio VERIFIED y no suspendido.
 * Excluye phone, rejectionReason, suspended, actorId.
 * Incluye address porque es la dirección del negocio (no PII del individuo).
 */
public record PublicMerchantView(
        AccountId accountId,
        String businessName,
        BusinessCategory category,
        String address,
        double latitude,
        double longitude,
        String description
) {
}