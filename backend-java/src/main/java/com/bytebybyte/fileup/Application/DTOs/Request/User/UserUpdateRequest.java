package com.bytebybyte.fileup.Application.DTOs.Request.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(

        @Size(
                min = 2,
                max = 20,
                message = "Fist name must range between 2 and 20 characters"
        )
        String firstName,


        @Size(
                min = 2,
                max = 20,
                message = "Second must range between 2 and 20 characters"
        )
        String secondName,


        @Email(message = "Email must be in valid format")
        @Size(max = 100, message = "Email must have at most 100 characters")
        @Pattern(
                regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                message = "Email must contain a valid domain"
        )
        String email,


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
