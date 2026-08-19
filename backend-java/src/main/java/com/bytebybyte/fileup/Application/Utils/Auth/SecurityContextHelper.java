package com.bytebybyte.fileup.Application.Utils.Auth;

import com.bytebybyte.fileup.Domain.Exceptions.UnauthorizedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SecurityContextHelper {

    public UUID getCurrentUserId() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                authentication instanceof AnonymousAuthenticationToken) {

            throw new UnauthorizedException(
                    "User is not authenticated.",
                    "SecurityContextHelper_getCurrentUserId"
            );
        }

        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException exception) {

            throw new UnauthorizedException(
                    "Invalid authenticated user ID.",
                    "SecurityContextHelper_getCurrentUserId"
            );
        }
    }
}
