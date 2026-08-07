package com.bytebybyte.fileup.Application.DTOs.Response.Auth;

/**
 * Login response DTO.
 * @param accessToken
 * @param expiresIn
 */
public record LoginResponse(
        String accessToken,
        java.time.Instant expiresIn) {}
