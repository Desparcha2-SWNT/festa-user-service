package com.festa.user.domain.exception;


public class NotFoundException extends DomainException {

    public NotFoundException(String message) {
        super("NOT_FOUND", message);
    }

    public static NotFoundException of(String resource, Object id) {
        return new NotFoundException(resource + " no encontrado: " + id);
    }
}