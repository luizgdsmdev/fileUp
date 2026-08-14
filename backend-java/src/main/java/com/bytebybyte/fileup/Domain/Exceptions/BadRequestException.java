package com.bytebybyte.fileup.Domain.Exceptions;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseApiException {
    public BadRequestException(String message, String issuer) {
        super(message, HttpStatus.BAD_REQUEST, issuer);
    }
}
