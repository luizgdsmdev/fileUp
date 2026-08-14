package com.bytebybyte.fileup.Domain.Exceptions;

import org.springframework.http.HttpStatus;

public abstract class BaseApiException extends RuntimeException {

    private final HttpStatus status;
    private String issuer;

    protected BaseApiException(String message, HttpStatus status, String issuer) {
        super(message);
        this.status = status;
        this.issuer = issuer;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getIssuer() { return issuer; }
}