package com.bytebybyte.fileup.Controller.User;


import com.bytebybyte.fileup.Application.DTOs.Request.User.UserUpdateRequest;
import com.bytebybyte.fileup.Application.DTOs.Response.User.UserResponse;
import com.bytebybyte.fileup.Application.Services.User.UserService;
import com.bytebybyte.fileup.Controllers.User.UserController;
import com.bytebybyte.fileup.Domain.Exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService _userService;

    @InjectMocks
    private UserController _userController;


    // Get method
    @Test
    void shouldReturnUserSuccessfully() {

        // Arrange
        UUID userId = UUID.randomUUID();

        UserResponse userResponse =
                mock(UserResponse.class);

        when(_userService.get(userId))
                .thenReturn(
                        ResponseEntity.ok(userResponse)
                );

        // Act
        ResponseEntity<UserResponse> response =
                _userController.getUser(userId);

        // Assert
        assertNotNull(response);

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertEquals(
                userResponse,
                response.getBody()
        );

        // Verify
        verify(_userService)
                .get(userId);
    }

    @Test
    void shouldPropagateNotFoundException() {

        // Arrange
        UUID userId = UUID.randomUUID();

        when(_userService.get(userId))
                .thenThrow(
                        new NotFoundException(
                                "No user found with this ID.",
                                "UserController_getUser"
                        )
                );

        // Act + Assert
        NotFoundException exception =
                assertThrows(
                        NotFoundException.class,
                        () -> _userController.getUser(userId)
                );

        assertEquals(
                "No user found with this ID.",
                exception.getMessage()
        );

        // Verify
        verify(_userService)
                .get(userId);
    }


    // Patch method
    @Test
    void shouldUpdateUserSuccessfully() {

        // Arrange
        UUID userId = UUID.randomUUID();

        UserUpdateRequest request =
                new UserUpdateRequest(
                        "Luiz",
                        null,
                        null,
                        null
                );

        UserResponse userResponse =
                mock(UserResponse.class);

        when(_userService.update(
                userId,
                request
        )).thenReturn(
                ResponseEntity.ok(userResponse)
        );

        // Act
        ResponseEntity<UserResponse> response =
                _userController.updateUser(
                        userId,
                        request
                );

        // Assert
        assertNotNull(response);

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertEquals(
                userResponse,
                response.getBody()
        );

        // Verify
        verify(_userService)
                .update(
                        userId,
                        request
                );
    }

    @Test
    void shouldPropagateNotFoundExceptionWhenUpdatingUser() {

        // Arrange
        UUID userId = UUID.randomUUID();

        UserUpdateRequest request =
                new UserUpdateRequest(
                        "Luiz",
                        null,
                        null,
                        null
                );

        when(_userService.update(
                userId,
                request
        )).thenThrow(
                new NotFoundException(
                        "No user found with this ID.",
                        "UserController_updateUser"
                )
        );

        // Act + Assert
        NotFoundException exception =
                assertThrows(
                        NotFoundException.class,
                        () -> _userController.updateUser(
                                userId,
                                request
                        )
                );

        assertEquals(
                "No user found with this ID.",
                exception.getMessage()
        );

        // Verify
        verify(_userService)
                .update(
                        userId,
                        request
                );
    }
}
