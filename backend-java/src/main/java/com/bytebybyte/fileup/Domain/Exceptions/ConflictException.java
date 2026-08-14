package com.bytebybyte.fileup.Domain.Exceptions;

import org.springframework.http.HttpStatus;

public class ConflictException extends BaseApiException {
    public ConflictException(String message, String issuer) {
        super(message, HttpStatus.CONFLICT, issuer);
    }
}
