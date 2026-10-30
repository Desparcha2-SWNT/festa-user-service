package com.festa.user.domain.exception;

import java.util.List;

public class ValidationException extends DomainException {

    private final List<FieldError> errors;

    public ValidationException(String message, List<FieldError> errors) {
        super("VALIDATION_ERROR", message);
        this.errors = List.copyOf(errors);
    }

    public ValidationException(FieldError... errors) {
        this("Algunos campos no cumplen las validaciones", List.of(errors));
    }

    public List<FieldError> getErrors() {
        return errors;
    }

    public record FieldError(String field, String message) {}
}