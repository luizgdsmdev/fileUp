package com.bytebybyte.fileup.Controller.Auth;

import com.bytebybyte.fileup.Application.DTOs.Request.Auth.LoginRequest;
import com.bytebybyte.fileup.Application.DTOs.Request.Auth.RegisterRequest;
import com.bytebybyte.fileup.Application.DTOs.Response.Auth.LoginResponse;
import com.bytebybyte.fileup.Application.Services.Auth.AuthService;
import com.bytebybyte.fileup.Controllers.Auth.AuthController;
import com.bytebybyte.fileup.Domain.Exceptions.ConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import java.net.URI;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {
    @Mock
    private AuthService _authService;

    @InjectMocks
    private AuthController _authController;

    // Login method
    @Test
    void shouldReturnOkWhenLoginIsSuccessful() {

        // Arrange
        LoginRequest request = new LoginRequest(
                "luiz@email.com",
                "qwQW12!@"
        );

        LoginResponse loginResponse =
                mock(LoginResponse.class);

        when(_authService.login(request))
                .thenReturn(
                        ResponseEntity.ok(loginResponse)
                );

        // Act
        ResponseEntity<LoginResponse> response =
                _authController.login(request);

        // Assert
        assertNotNull(response);

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertEquals(
                loginResponse,
                response.getBody()
        );

        // Verify
        verify(_authService)
                .login(request);
    }


    @Test
    void shouldPropagateBadCredentialsException() {

        // Arrange
        LoginRequest request = new LoginRequest(
                "luiz@email.com",
                "wrongPassword"
        );

        BadCredentialsException exception =
                new BadCredentialsException(
                        "Invalid credentials for this user."
                );

        when(_authService.login(request))
                .thenThrow(exception);

        // Act + Assert
        BadCredentialsException thrownException =
                assertThrows(
                        BadCredentialsException.class,
                        () -> _authController.login(request)
                );

        assertEquals(
                "Invalid credentials for this user.",
                thrownException.getMessage()
        );

        // Verify
        verify(_authService)
                .login(request);
    }


    // Register method
    @Test
    void shouldReturnCreatedWhenRegistrationIsSuccessful() {

        // Arrange
        RegisterRequest request = new RegisterRequest(
                "Luiz",
                "Gustavo",
                "luiz@email.com",
                "qwQW12!@"
        );

        LoginResponse loginResponse =
                mock(LoginResponse.class);

        UUID userId = UUID.randomUUID();

        URI location =
                URI.create(
                        "/api/v1/user/" + userId
                );

        ResponseEntity<LoginResponse> expectedResponse =
                ResponseEntity
                        .created(location)
                        .body(loginResponse);

        when(_authService.register(request))
                .thenReturn(expectedResponse);

        // Act
        ResponseEntity<LoginResponse> response =
                _authController.register(request);

        // Assert
        assertNotNull(response);

        assertEquals(
                HttpStatus.CREATED,
                response.getStatusCode()
        );

        assertEquals(
                loginResponse,
                response.getBody()
        );

        assertEquals(
                location,
                response.getHeaders().getLocation()
        );

        // Verify
        verify(_authService)
                .register(request);
    }


    @Test
    void shouldPropagateConflictException() {

        // Arrange
        RegisterRequest request = new RegisterRequest(
                "Luiz",
                "Gustavo",
                "luiz@email.com",
                "qwQW12!@"
        );

        ConflictException exception =
                new ConflictException(
                        "Unable to use this data to create a user",
                        "AuthController_register_method"
                );

        when(_authService.register(request))
                .thenThrow(exception);

        // Act + Assert
        ConflictException thrownException =
                assertThrows(
                        ConflictException.class,
                        () -> _authController.register(request)
                );

        assertEquals(
                "Unable to use this data to create a user",
                thrownException.getMessage()
        );

        // Verify
        verify(_authService)
                .register(request);
    }

}
