package com.festa.user.domain.exception;

/**
 * Se traduce a HTTP 409. Acepta un code específico para casos como
 * MERCHANT_ALREADY_EXISTS o INVALID_STATE_TRANSITION.
 */
public class ConflictException extends DomainException {

    public ConflictException(String message) {
        super("CONFLICT", message);
    }

    public ConflictException(String code, String message) {
        super(code, message);
    }

    public static ConflictException invalidTransition(String from, String to) {
        return new ConflictException(
                "INVALID_STATE_TRANSITION",
                "Transición de estado no permitida: " + from + " -> " + to
        );
    }

    public static ConflictException merchantAlreadyExists() {
        return new ConflictException(
                "MERCHANT_ALREADY_EXISTS",
                "Ya existe un perfil de comercio para esta cuenta"
        );
    }
}