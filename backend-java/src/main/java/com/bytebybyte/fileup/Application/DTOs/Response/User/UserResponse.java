package com.bytebybyte.fileup.Application.DTOs.Response.User;

import java.util.UUID;

public record UserResponse(
        String firstName,
        String secondName,
        String userEmail,
        UUID userId) {}
