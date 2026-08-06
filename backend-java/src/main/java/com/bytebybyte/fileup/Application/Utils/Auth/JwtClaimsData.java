package com.bytebybyte.fileup.Application.Utils.Auth;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;

import java.time.Instant;

public record JwtClaimsData(
        JwtClaimsSet claims,
        Instant expiration
) {}