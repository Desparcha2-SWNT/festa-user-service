package com.festa.user.domain.exception;


public class ForbiddenException extends DomainException {

    public ForbiddenException(String message) {
        super("FORBIDDEN", message);
    }

    public ForbiddenException(String code, String message) {
        super(code, message);
    }
}