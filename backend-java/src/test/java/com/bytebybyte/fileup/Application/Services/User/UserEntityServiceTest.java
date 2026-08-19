package com.bytebybyte.fileup.Application.Services.User;

import com.bytebybyte.fileup.Application.DTOs.Request.User.UserUpdateRequest;
import com.bytebybyte.fileup.Application.DTOs.Response.User.UserResponse;
import com.bytebybyte.fileup.Application.Mappings.User.UserMapping;
import com.bytebybyte.fileup.Domain.Entities.User.UserEntity;
import com.bytebybyte.fileup.Domain.Exceptions.ConflictException;
import com.bytebybyte.fileup.Domain.Exceptions.NotFoundException;
import com.bytebybyte.fileup.Infrastructure.Persistence.Interfaces.Users.IUserRepository;
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
public class UserEntityServiceTest {

    @Mock
    private IUserRepository _I_userRepository;

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

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setEmail("luiz@email.com");
        userEntity.setFirstName("Luiz");
        userEntity.setSecondName("Gustavo");

        when(_I_userRepository.findById(userId))
                .thenReturn(Optional.of(userEntity));
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

        verify(_I_userRepository).findById(userId);
    }

    @Test
    public void testShouldThrowNotFoundExceptionWhenUserDoesNotExist() {

        // Arrange
        UUID userId = UUID.fromString(
                "089d9a44-5ecd-4c23-85e1-22047e1d8225"
        );

        when(_I_userRepository.findById(userId))
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

        verify(_I_userRepository).findById(userId);
    }

    // Update method - email conflict path
    @Test
    void shouldThrowConflictExceptionWhenEmailAlreadyUsed() {

        // Arrange
        UUID userId = UUID.randomUUID();

        UserEntity existingUserEntity = new UserEntity();
        existingUserEntity.setId(userId);
        existingUserEntity.setFirstName("Luiz");
        existingUserEntity.setSecondName("Gustavo");
        existingUserEntity.setEmail("old@mail.com");
        existingUserEntity.setPassword(
                _bCryptPasswordEncoder.encode("qwQW12!@")
        );

        UserEntity anotherUserEntityWithEmail = new UserEntity();
        anotherUserEntityWithEmail.setId(UUID.randomUUID());
        anotherUserEntityWithEmail.setEmail("new@mail.com");

        UserUpdateRequest request = new UserUpdateRequest(
                null,
                null,
                "new@mail.com",
                null
        );

        when(_I_userRepository.findById(userId))
                .thenReturn(Optional.of(existingUserEntity));

        when(_I_userRepository.findByEmail("new@mail.com"))
                .thenReturn(Optional.of(anotherUserEntityWithEmail));

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

        verify(_I_userRepository).findById(userId);

        verify(_I_userRepository).findByEmail("new@mail.com");

        verify(_I_userRepository, never())
                .save(any(UserEntity.class));
    }


    @Test
    void shouldKeepCurrentEmailWhenEmailBelongsToSameUser() {

        // Arrange
        UUID userId = UUID.randomUUID();

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setFirstName("Luiz");
        userEntity.setSecondName("Gustavo");
        userEntity.setEmail("email@mail.com");
        userEntity.setPassword(
                _bCryptPasswordEncoder.encode("qwQW12!@")
        );

        String originalEmail = userEntity.getEmail();

        UserUpdateRequest request = new UserUpdateRequest(
                null,
                null,
                "email@mail.com",
                null
        );

        when(_I_userRepository.findById(userId))
                .thenReturn(Optional.of(userEntity));

        when(_I_userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ResponseEntity<UserResponse> response =
                _userService.update(userId, request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());

        assertEquals(
                originalEmail,
                userEntity.getEmail()
        );

        verify(_I_userRepository).findById(userId);

        verify(_I_userRepository, never())
                .findByEmail(anyString());

        verify(_I_userRepository).save(userEntity);
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

        when(_I_userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> _userService.update(userId, request)
        );

        verify(_I_userRepository, never())
                .save(any(UserEntity.class));
    }


    @Test
    void shouldUpdateFirstNameOnly() {

        // Arrange
        UUID userId = UUID.randomUUID();

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setFirstName("Luiz");
        userEntity.setSecondName("Gustavo");
        userEntity.setEmail("email@email.com");
        userEntity.setPassword(
                _bCryptPasswordEncoder.encode("qwQW12!@")
        );

        String originalEmail = userEntity.getEmail();
        String originalSecondName = userEntity.getSecondName();
        String originalPassword = userEntity.getPassword();

        UserUpdateRequest request = new UserUpdateRequest(
                "João",
                null,
                null,
                null
        );

        when(_I_userRepository.findById(userId))
                .thenReturn(Optional.of(userEntity));

        when(_I_userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ResponseEntity<UserResponse> response =
                _userService.update(userId, request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());

        ArgumentCaptor<UserEntity> userCaptor =
                ArgumentCaptor.forClass(UserEntity.class);

        verify(_I_userRepository).save(userCaptor.capture());

        UserEntity savedUserEntity = userCaptor.getValue();

        // Field that SHOULD change
        assertEquals(
                "João",
                savedUserEntity.getFirstName()
        );

        // Fields that SHOULD NOT change
        assertEquals(originalEmail, savedUserEntity.getEmail());
        assertEquals(originalSecondName, savedUserEntity.getSecondName());
        assertEquals(originalPassword, savedUserEntity.getPassword());
    }


    @Test
    void shouldUpdateSecondNameOnly() {

        // Arrange
        UUID userId = UUID.randomUUID();

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setFirstName("Luiz");
        userEntity.setSecondName("Gustavo");
        userEntity.setEmail("email@email.com");
        userEntity.setPassword(
                _bCryptPasswordEncoder.encode("qwQW12!@")
        );

        String originalEmail = userEntity.getEmail();
        String originalFirstName = userEntity.getFirstName();
        String originalPassword = userEntity.getPassword();

        UserUpdateRequest request = new UserUpdateRequest(
                null,
                "João",
                null,
                null
        );

        when(_I_userRepository.findById(userId))
                .thenReturn(Optional.of(userEntity));

        when(_I_userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ResponseEntity<UserResponse> response =
                _userService.update(userId, request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());

        ArgumentCaptor<UserEntity> userCaptor =
                ArgumentCaptor.forClass(UserEntity.class);

        verify(_I_userRepository).save(userCaptor.capture());

        UserEntity savedUserEntity = userCaptor.getValue();

        // Field that SHOULD change
        assertEquals(
                "João",
                savedUserEntity.getSecondName()
        );

        // Fields that SHOULD NOT change
        assertEquals(originalEmail, savedUserEntity.getEmail());
        assertEquals(originalFirstName, savedUserEntity.getFirstName());
        assertEquals(originalPassword, savedUserEntity.getPassword());
    }


    @Test
    void shouldUpdateEmailOnly() {

        // Arrange
        UUID userId = UUID.randomUUID();

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setFirstName("Luiz");
        userEntity.setSecondName("Gustavo");
        userEntity.setEmail("email@email.com");
        userEntity.setPassword(
                _bCryptPasswordEncoder.encode("qwQW12!@")
        );

        String originalFirstName = userEntity.getFirstName();
        String originalSecondName = userEntity.getSecondName();
        String originalPassword = userEntity.getPassword();

        UserUpdateRequest request = new UserUpdateRequest(
                null,
                null,
                "new@gmail.com",
                null
        );

        when(_I_userRepository.findById(userId))
                .thenReturn(Optional.of(userEntity));

        when(_I_userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ResponseEntity<UserResponse> response =
                _userService.update(userId, request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());

        ArgumentCaptor<UserEntity> userCaptor =
                ArgumentCaptor.forClass(UserEntity.class);

        verify(_I_userRepository).save(userCaptor.capture());

        UserEntity savedUserEntity = userCaptor.getValue();

        // Field that SHOULD change
        assertEquals(
                "new@gmail.com",
                savedUserEntity.getEmail()
        );

        // Fields that SHOULD NOT change
        assertEquals(originalFirstName, savedUserEntity.getFirstName());
        assertEquals(originalSecondName, savedUserEntity.getSecondName());
        assertEquals(originalPassword, savedUserEntity.getPassword());
    }


    @Test
    void shouldUpdatePasswordOnly() {

        // Arrange
        UUID userId = UUID.randomUUID();

        String originalPassword = "qwQW12!@";
        String newPassword = "newPassword";

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setFirstName("Luiz");
        userEntity.setSecondName("Gustavo");
        userEntity.setEmail("email@email.com");
        userEntity.setPassword(
                _bCryptPasswordEncoder.encode(originalPassword)
        );

        String originalFirstName = userEntity.getFirstName();
        String originalSecondName = userEntity.getSecondName();
        String originalEmail = userEntity.getEmail();

        UserUpdateRequest request = new UserUpdateRequest(
                null,
                null,
                null,
                newPassword
        );

        when(_I_userRepository.findById(userId))
                .thenReturn(Optional.of(userEntity));

        when(_I_userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ResponseEntity<UserResponse> response =
                _userService.update(userId, request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());

        ArgumentCaptor<UserEntity> userCaptor =
                ArgumentCaptor.forClass(UserEntity.class);

        verify(_I_userRepository).save(userCaptor.capture());

        UserEntity savedUserEntity = userCaptor.getValue();

        // Password SHOULD change
        assertTrue(
                _bCryptPasswordEncoder.matches(
                        newPassword,
                        savedUserEntity.getPassword()
                )
        );

        // Old password SHOULD NOT work anymore
        assertFalse(
                _bCryptPasswordEncoder.matches(
                        originalPassword,
                        savedUserEntity.getPassword()
                )
        );

        // Other fields SHOULD NOT change
        assertEquals(originalFirstName, savedUserEntity.getFirstName());
        assertEquals(originalSecondName, savedUserEntity.getSecondName());
        assertEquals(originalEmail, savedUserEntity.getEmail());
    }


    @Test
    void shouldPreserveAllFieldsWhenNoFieldIsProvided() {

        UUID userId = UUID.randomUUID();

        String password = "qwQW12!@";

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setFirstName("Luiz");
        userEntity.setSecondName("Gustavo");
        userEntity.setEmail("email@email.com");
        userEntity.setPassword(
                _bCryptPasswordEncoder.encode(password)
        );

        UserUpdateRequest request = new UserUpdateRequest(
                null,
                null,
                null,
                null
        );

        when(_I_userRepository.findById(userId))
                .thenReturn(Optional.of(userEntity));

        when(_I_userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        _userService.update(userId, request);

        // Assert
        ArgumentCaptor<UserEntity> userCaptor =
                ArgumentCaptor.forClass(UserEntity.class);

        verify(_I_userRepository).save(userCaptor.capture());

        UserEntity savedUserEntity = userCaptor.getValue();

        assertEquals("Luiz", savedUserEntity.getFirstName());
        assertEquals("Gustavo", savedUserEntity.getSecondName());
        assertEquals("email@email.com", savedUserEntity.getEmail());

        assertTrue(
                _bCryptPasswordEncoder.matches(
                        password,
                        savedUserEntity.getPassword()
                )
        );
    }
}