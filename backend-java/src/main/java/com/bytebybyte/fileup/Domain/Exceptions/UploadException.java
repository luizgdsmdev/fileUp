package com.bytebybyte.fileup.Domain.Exceptions;

import org.springframework.http.HttpStatus;

public class UploadException extends BaseApiException {
    public UploadException(String message, String issuer) {super(message, HttpStatus.SERVICE_UNAVAILABLE, issuer);}
}
