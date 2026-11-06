package com.festa.user.application.command;

import com.festa.user.domain.vo.AccountId;

/**
 * Campos null = no cambiar; string vacío = limpiar.
 * El {@code phone} viaja en claro; el cifrado se aplica en persistencia.
 */
public record UpdateProfileCommand(
        AccountId accountId,
        String bio,
        String avatarUrl,
        String phone
) {
}