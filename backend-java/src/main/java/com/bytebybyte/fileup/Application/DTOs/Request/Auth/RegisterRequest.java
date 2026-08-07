package com.bytebybyte.fileup.Application.DTOs.Request.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Fist name is required")
        @Size(
                min = 2,
                max = 20,
                message = "Fist name must range between 2 and 20 characters"
        )
        String firstName,


        @NotBlank(message = "Second name is required")
        @Size(
                min = 2,
                max = 20,
                message = "Second must range between 2 and 20 characters"
        )
        String secondName,


        @NotBlank(message = "Email is required")
        @Email(message = "Email must be in valid format")
        @Size(max = 100, message = "Email must have at most 100 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(
                min = 8,
                max = 100,
                message = "Password must range between 8 and 100 characters"
        )
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=]).*$",
                message = "The password must contain uppercase and lowercase letters, a number, and a special character."
        )
        String password
) {
}
