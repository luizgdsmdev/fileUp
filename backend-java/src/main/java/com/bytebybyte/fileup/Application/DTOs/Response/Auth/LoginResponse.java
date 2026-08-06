package com.bytebybyte.fileup.Application.DTOs.Response.Auth;

public record LoginResponse(String accessToken, java.time.Instant expiresIn) {}
