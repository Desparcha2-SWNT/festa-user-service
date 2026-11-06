package com.festa.user.domain.port.in;

import com.festa.user.domain.model.Consent;
import com.festa.user.domain.vo.AccountId;

import java.util.List;

/**
 * Devuelve el último consentimiento registrado por tipo.
 * Los tipos nunca registrados no aparecen (el cliente los asume "no otorgado").
 */
public interface ListConsentsUseCase {
    List<Consent> handle(AccountId accountId);
}