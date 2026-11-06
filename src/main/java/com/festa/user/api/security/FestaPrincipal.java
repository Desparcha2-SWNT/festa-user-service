package com.festa.user.api.security;

import com.festa.user.domain.vo.AccountId;

import java.security.Principal;

/**
 * Principal de dominio construido desde el Jwt validado. {@code accountId}
 * es el claim {@code sub}. {@code role} es el claim {@code role} de auth
 * (user | merchant | admin). El user-service nunca confía en {@code role}
 * para autorizar recursos propios: solo lo usa en endpoints /admin/*.
 */
public record FestaPrincipal(AccountId accountId, String role) implements Principal {

    @Override
    public String getName() {
        return accountId.value().toString();
    }
}