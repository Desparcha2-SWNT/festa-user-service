package com.festa.user.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(ErrorBody error) {

    public record ErrorBody(String code, String message, List<Detail> details) {
    }

    public record Detail(String field, String message) {
    }

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(new ErrorBody(code, message, null));
    }

    public static ErrorResponse of(String code, String message, List<Detail> details) {
        return new ErrorResponse(new ErrorBody(code, message, details));
    }
}