package com.bytebybyte.fileup.Domain.Exceptions;

import org.springframework.http.HttpStatus;

public class TokenGenerationException extends BaseApiException  {
    public TokenGenerationException(String message, String issuer) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE, issuer);
    }
}
