package com.festa.user.api.dto.request;

import jakarta.validation.constraints.Size;

/**
 * PATCH semántico: null = no cambiar; string vacío = limpiar.
 * La validación de tamaño solo aplica si el campo no es null.
 */
public record UpdateProfileRequest(
        @Size(max = 500) String bio,
        @Size(max = 500) String avatarUrl,
        @Size(max = 30) String phone
) {
}