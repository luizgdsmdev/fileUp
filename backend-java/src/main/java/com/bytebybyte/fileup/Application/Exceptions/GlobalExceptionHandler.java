package com.bytebybyte.fileup.Application.Exceptions;

import com.bytebybyte.fileup.Application.DTOs.Response.Erros.ErrorResponse;
import com.bytebybyte.fileup.Domain.Exceptions.BaseApiException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Global exception handler for personalize API exceptions. Vide Domain/Exceptions/
     * @param ex status code from exception
     * @param request message from exception
     * @return ResponseEntity<ErrorResponse>, being ErrorResponse a DTO
     */
    @ExceptionHandler(BaseApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(
            BaseApiException ex,
            HttpServletRequest request
    ) {

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                ex.getStatus().value(),
                ex.getStatus().getReasonPhrase(),
                ex.getMessage() != null ? ex.getMessage() : "Internal server error",
                request.getRequestURI(),
                null
        );


        return ResponseEntity
                .status(ex.getStatus())
                .body(response);
    }


    /**
     * Global exception handler for general MethodArgumentNotValidException exception.
     * Mainly for controller validation.
     * @param ex status code from exception
     * @param request message from exception
     * @return ResponseEntity<ErrorResponse>, being ErrorResponse a DTO
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        Map<String, String> errors = new HashMap<>();

        assert ex.getBindingResult() != null;
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );


        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage() != null ? ex.getMessage() :  "Validation error:",
                request.getRequestURI(),
                errors
        );


        return ResponseEntity
                .badRequest()
                .body(response);
    }


    /**
     * Global exception handler for BadCredentialsException exception.
     * Mainly for authentication, avoid exposing sensitive information.
     * @param ex status code from exception
     * @param request message from exception
     * @return ResponseEntity<ErrorResponse>, being ErrorResponse a DTO
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request
    ) {

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                ex.getMessage() != null ? ex.getMessage() : "Invalid credentials for this user.",
                request.getRequestURI(),
                null
        );


        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }
}
