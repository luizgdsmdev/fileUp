package com.bytebybyte.fileup.Application.DTOs.Response.Auth;

import java.time.Instant;

/**
 * Login response DTO.
 * @param accessToken
 * @param expiresIn
 */
public record LoginResponse(
        String accessToken,
        Instant expiresIn) {}
