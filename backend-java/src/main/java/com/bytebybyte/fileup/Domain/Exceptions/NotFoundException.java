package com.bytebybyte.fileup.Domain.Exceptions;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BaseApiException  {
    public NotFoundException(String message, String issuer) {
        super(message, HttpStatus.NOT_FOUND, issuer);
    }
}
