package com.bytebybyte.fileup.Application.Services.User;

import com.bytebybyte.fileup.Application.DTOs.Request.User.UserUpdateRequest;
import com.bytebybyte.fileup.Application.DTOs.Response.User.UserResponse;
import com.bytebybyte.fileup.Application.Mappings.User.UserMapping;
import com.bytebybyte.fileup.Domain.Entities.User.User;
import com.bytebybyte.fileup.Domain.Exceptions.ConflictException;
import com.bytebybyte.fileup.Domain.Exceptions.NotFoundException;
import com.bytebybyte.fileup.Infrastructure.Persistence.Interfaces.User.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository _userRepository;

    @Spy
    private UserMapping _userMapping = new UserMapping();

    @Spy
    private BCryptPasswordEncoder _bCryptPasswordEncoder =
            new BCryptPasswordEncoder();

    @InjectMocks
    private UserService _userService;


    // GET method
    @Test
    public void testShouldReturnResponseEntityWithUserResponse() {

        // Arrange data for mockito ----
        UUID userId = UUID.fromString("089d9a44-5ecd-4c23-85e1-22047e1d8225");

        User user = new User();
        user.setId(userId);
        user.setEmail("luiz@email.com");
        user.setFirstName("Luiz");
        user.setSecondName("Gustavo");

        when(_userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        // Arrange data for mockito ----


        // Act
        ResponseEntity<UserResponse> response =
                _userService.get(userId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        assertEquals(userId, response.getBody().userId());
        assertEquals("luiz@email.com", response.getBody().userEmail());
        assertEquals("Luiz", response.getBody().firstName());
        assertEquals("Gustavo", response.getBody().secondName());

        verify(_userRepository).findById(userId);
    }

    @Test
    public void testShouldThrowNotFoundExceptionWhenUserDoesNotExist() {

        // Arrange
        UUID userId = UUID.fromString(
                "089d9a44-5ecd-4c23-85e1-22047e1d8225"
        );

        when(_userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // Act + Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> _userService.get(userId)
        );

        assertEquals(
                "No user found with this ID.",
                exception.getMessage()
        );

        verify(_userRepository).findById(userId);
    }

    // Update method - email conflict path
    @Test
    void shouldThrowConflictExceptionWhenEmailAlreadyUsed() {

        // Arrange
        UUID userId = UUID.randomUUID();

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setFirstName("Luiz");
        existingUser.setSecondName("Gustavo");
        existingUser.setEmail("old@mail.com");
        existingUser.setPassword(
                _bCryptPasswordEncoder.encode("qwQW12!@")
        );

        User anotherUserWithEmail = new User();
        anotherUserWithEmail.setId(UUID.randomUUID());
        anotherUserWithEmail.setEmail("new@mail.com");

        UserUpdateRequest request = new UserUpdateRequest(
                null,
                null,
                "new@mail.com",
                null
        );

        when(_userRepository.findById(userId))
                .thenReturn(Optional.of(existingUser));

        when(_userRepository.findByEmail("new@mail.com"))
                .thenReturn(Optional.of(anotherUserWithEmail));

        // Act
        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> _userService.update(userId, request)
        );

        // Assert
        assertEquals(
                "This email cannot be used.",
                exception.getMessage()
        );

        verify(_userRepository).findById(userId);

        verify(_userRepository).findByEmail("new@mail.com");

        verify(_userRepository, never())
                .save(any(User.class));
    }


    @Test
    void shouldKeepCurrentEmailWhenEmailBelongsToSameUser() {

        // Arrange
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setFirstName("Luiz");
        user.setSecondName("Gustavo");
        user.setEmail("email@mail.com");
        user.setPassword(
                _bCryptPasswordEncoder.encode("qwQW12!@")
        );

        String originalEmail = user.getEmail();

        UserUpdateRequest request = new UserUpdateRequest(
                null,
                null,
                "email@mail.com",
                null
        );

        when(_userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(_userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ResponseEntity<UserResponse> response =
                _userService.update(userId, request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());

        assertEquals(
                originalEmail,
                user.getEmail()
        );

        verify(_userRepository).findById(userId);

        verify(_userRepository, never())
                .findByEmail(anyString());

        verify(_userRepository).save(user);
    }
    // FETCH method for user updates
    @Test
    void shouldThrowNotFoundExceptionWhenUpdatingNonExistingUser() {

        // The update method finds the user by the ID and updates the fields
        UUID userId = UUID.randomUUID();

        UserUpdateRequest request = new UserUpdateRequest(
                null,
                null,
                null,
                null
        );

        when(_userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> _userService.update(userId, request)
        );

        verify(_userRepository, never())
                .save(any(User.class));
    }


    @Test
    void shouldUpdateFirstNameOnly() {

        // Arrange
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setFirstName("Luiz");
        user.setSecondName("Gustavo");
        user.setEmail("email@email.com");
        user.setPassword(
                _bCryptPasswordEncoder.encode("qwQW12!@")
        );

        String originalEmail = user.getEmail();
        String originalSecondName = user.getSecondName();
        String originalPassword = user.getPassword();

        UserUpdateRequest request = new UserUpdateRequest(
                "João",
                null,
                null,
                null
        );

        when(_userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(_userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ResponseEntity<UserResponse> response =
                _userService.update(userId, request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(_userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        // Field that SHOULD change
        assertEquals(
                "João",
                savedUser.getFirstName()
        );

        // Fields that SHOULD NOT change
        assertEquals(originalEmail,savedUser.getEmail());
        assertEquals(originalSecondName,savedUser.getSecondName());
        assertEquals(originalPassword,savedUser.getPassword());
    }


    @Test
    void shouldUpdateSecondNameOnly() {

        // Arrange
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setFirstName("Luiz");
        user.setSecondName("Gustavo");
        user.setEmail("email@email.com");
        user.setPassword(
                _bCryptPasswordEncoder.encode("qwQW12!@")
        );

        String originalEmail = user.getEmail();
        String originalFirstName = user.getFirstName();
        String originalPassword = user.getPassword();

        UserUpdateRequest request = new UserUpdateRequest(
                null,
                "João",
                null,
                null
        );

        when(_userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(_userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ResponseEntity<UserResponse> response =
                _userService.update(userId, request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(_userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        // Field that SHOULD change
        assertEquals(
                "João",
                savedUser.getSecondName()
        );

        // Fields that SHOULD NOT change
        assertEquals(originalEmail,savedUser.getEmail());
        assertEquals(originalFirstName,savedUser.getFirstName());
        assertEquals(originalPassword,savedUser.getPassword());
    }


    @Test
    void shouldUpdateEmailOnly() {

        // Arrange
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setFirstName("Luiz");
        user.setSecondName("Gustavo");
        user.setEmail("email@email.com");
        user.setPassword(
                _bCryptPasswordEncoder.encode("qwQW12!@")
        );

        String originalFirstName = user.getFirstName();
        String originalSecondName = user.getSecondName();
        String originalPassword = user.getPassword();

        UserUpdateRequest request = new UserUpdateRequest(
                null,
                null,
                "new@gmail.com",
                null
        );

        when(_userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(_userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ResponseEntity<UserResponse> response =
                _userService.update(userId, request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(_userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        // Field that SHOULD change
        assertEquals(
                "new@gmail.com",
                savedUser.getEmail()
        );

        // Fields that SHOULD NOT change
        assertEquals(originalFirstName,savedUser.getFirstName());
        assertEquals(originalSecondName,savedUser.getSecondName());
        assertEquals(originalPassword,savedUser.getPassword());
    }


    @Test
    void shouldUpdatePasswordOnly() {

        // Arrange
        UUID userId = UUID.randomUUID();

        String originalPassword = "qwQW12!@";
        String newPassword = "newPassword";

        User user = new User();
        user.setId(userId);
        user.setFirstName("Luiz");
        user.setSecondName("Gustavo");
        user.setEmail("email@email.com");
        user.setPassword(
                _bCryptPasswordEncoder.encode(originalPassword)
        );

        String originalFirstName = user.getFirstName();
        String originalSecondName = user.getSecondName();
        String originalEmail = user.getEmail();

        UserUpdateRequest request = new UserUpdateRequest(
                null,
                null,
                null,
                newPassword
        );

        when(_userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(_userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ResponseEntity<UserResponse> response =
                _userService.update(userId, request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(_userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        // Password SHOULD change
        assertTrue(
                _bCryptPasswordEncoder.matches(
                        newPassword,
                        savedUser.getPassword()
                )
        );

        // Old password SHOULD NOT work anymore
        assertFalse(
                _bCryptPasswordEncoder.matches(
                        originalPassword,
                        savedUser.getPassword()
                )
        );

        // Other fields SHOULD NOT change
        assertEquals(originalFirstName, savedUser.getFirstName());
        assertEquals(originalSecondName, savedUser.getSecondName());
        assertEquals(originalEmail, savedUser.getEmail());
    }


    @Test
    void shouldPreserveAllFieldsWhenNoFieldIsProvided() {

        UUID userId = UUID.randomUUID();

        String password = "qwQW12!@";

        User user = new User();
        user.setId(userId);
        user.setFirstName("Luiz");
        user.setSecondName("Gustavo");
        user.setEmail("email@email.com");
        user.setPassword(
                _bCryptPasswordEncoder.encode(password)
        );

        UserUpdateRequest request = new UserUpdateRequest(
                null,
                null,
                null,
                null
        );

        when(_userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(_userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        _userService.update(userId, request);

        // Assert
        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(_userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals("Luiz", savedUser.getFirstName());
        assertEquals("Gustavo", savedUser.getSecondName());
        assertEquals("email@email.com", savedUser.getEmail());

        assertTrue(
                _bCryptPasswordEncoder.matches(
                        password,
                        savedUser.getPassword()
                )
        );
    }
}