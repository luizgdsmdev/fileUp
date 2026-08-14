package com.bytebybyte.fileup.Application.Services.Auth;

import com.bytebybyte.fileup.Application.DTOs.Request.Auth.LoginRequest;
import com.bytebybyte.fileup.Application.DTOs.Request.Auth.RegisterRequest;
import com.bytebybyte.fileup.Application.DTOs.Response.Auth.LoginResponse;
import com.bytebybyte.fileup.Application.Mappings.Auth.AuthMapping;
import com.bytebybyte.fileup.Domain.Entities.Roles.Role;
import com.bytebybyte.fileup.Domain.Entities.User.User;
import com.bytebybyte.fileup.Domain.Enums.Roles.RolesEnum;
import com.bytebybyte.fileup.Domain.Exceptions.BadRequestException;
import com.bytebybyte.fileup.Domain.Exceptions.ConflictException;
import com.bytebybyte.fileup.Domain.Exceptions.NotFoundException;
import com.bytebybyte.fileup.Infrastructure.Persistence.Interfaces.Roles.RolesRepository;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository _userRepository;

    @Mock
    private RolesRepository _roleRepository;

    @Mock
    private AuthMapping _authMapping;

    @Mock
    private JwtEncoder _jwtEncoder;

    @Spy
    private BCryptPasswordEncoder _bCryptPasswordEncoder =
            new BCryptPasswordEncoder();

    @InjectMocks
    private AuthService _authService;


    // Register method
    @Test
    void shouldRegisterUserSuccessfully() {

        // ARRANGE
        UUID userId = UUID.randomUUID();

        String email = "luiz@email.com";
        String password = "qwQW12!@";
        String fakeJwt = "fake.jwt.token";

        RegisterRequest request = new RegisterRequest(
                "Luiz",
                "Gustavo",
                email,
                password
        );

        // Role necessary for registration
        Role basicRole = new Role();
        basicRole.setName(
                RolesEnum.BASIC.getAuthority()
        );

        // User created by mapper
        User mappedUser = new User();

        // User saved in database
        User savedUser = new User();
        savedUser.setId(userId);

        // Expected response
        LoginResponse expectedLoginResponse =
                mock(LoginResponse.class);


        // User does not already exist
        when(_userRepository.findByEmail(email))
                .thenReturn(Optional.empty());


        // BASIC role exists
        when(_roleRepository.findByName(
                RolesEnum.BASIC.getAuthority()
        )).thenReturn(Optional.of(basicRole));


        // AuthMapping creates the User
        when(_authMapping.toUserEntity(
                eq(request),
                anyString(),
                anySet()
        )).thenReturn(mappedUser);


        // Repository saves the User
        when(_userRepository.save(mappedUser))
                .thenReturn(savedUser);


        // ---------------------------------------------------------
        // JWT generation
        //
        // register()
        //     -> _authHandler()
        //          -> _generateJwtClaimsSet()
        //          -> _jwtEncoder.encode()
        // ---------------------------------------------------------
        Jwt jwt = mock(Jwt.class);

        when(_jwtEncoder.encode(
                any(JwtEncoderParameters.class)
        )).thenReturn(jwt);

        when(jwt.getTokenValue())
                .thenReturn(fakeJwt);


        // Convert JWT into LoginResponse
        when(_authMapping.toLoginResponse(
                anyString(),
                any()
        )).thenReturn(expectedLoginResponse);


        // ACT -> register()
        ResponseEntity<LoginResponse> response =
                _authService.register(request);


        // ASSERT
        // Response exists
        assertNotNull(response);

        // HTTP 201 Created
        assertEquals(
                HttpStatus.CREATED,
                response.getStatusCode()
        );

        // Response body exists
        assertNotNull(response.getBody());

        // Response body is the expected LoginResponse
        assertEquals(
                expectedLoginResponse,
                response.getBody()
        );


        // Location header
        assertNotNull(response.getHeaders().getLocation());

        assertEquals(
                "/api/v1/user/" + userId,
                response.getHeaders()
                        .getLocation()
                        .toString());


        // VERIFY
        // Email was checked
        verify(_userRepository)
                .findByEmail(email);

        // BASIC role was retrieved
        verify(_roleRepository)
                .findByName(
                        RolesEnum.BASIC.getAuthority()
                );

        // User was saved
        verify(_userRepository)
                .save(mappedUser);

        // JWT was generated
        verify(_jwtEncoder)
                .encode(
                        any(JwtEncoderParameters.class)
                );

        // LoginResponse was generated
        verify(_authMapping)
                .toLoginResponse(
                        eq(fakeJwt),
                        any()
                );
    }


    @Test
    void shouldThrowConflictExceptionWhenEmailAlreadyExists() {

        // Arrange
        String email = "luiz@email.com";

        RegisterRequest request =
                new RegisterRequest(
                        "Luiz",
                        "Gustavo",
                        email,
                        "qwQW12!@"
                );

        User existingUser = new User();

        when(_userRepository.findByEmail(email))
                .thenReturn(Optional.of(existingUser));

        // Act + Assert
        ConflictException exception =
                assertThrows(
                        ConflictException.class,
                        () -> _authService.register(request)
                );

        assertEquals(
                "Unable to use this data to create a user",
                exception.getMessage()
        );

        verify(_userRepository)
                .findByEmail(email);

        verify(_roleRepository, never())
                .findByName(anyString());

        verify(_userRepository, never())
                .save(any(User.class));
    }


    @Test
    void shouldThrowNotFoundExceptionWhenBasicRoleDoesNotExist() {

        // Arrange
        RegisterRequest request =
                new RegisterRequest(
                        "Luiz",
                        "Gustavo",
                        "luiz@email.com",
                        "qwQW12!@"
                );

        when(_userRepository.findByEmail(
                request.email()
        )).thenReturn(Optional.empty());

        when(_roleRepository.findByName(
                RolesEnum.BASIC.getAuthority()
        )).thenReturn(Optional.empty());

        // Act + Assert
        NotFoundException exception =
                assertThrows(
                        NotFoundException.class,
                        () -> _authService.register(request)
                );

        assertEquals(
                "Role BASIC not fund.",
                exception.getMessage()
        );

        verify(_userRepository, never())
                .save(any(User.class));
    }


    @Test
    void shouldEncryptPasswordBeforeCreatingUser() {

        // Arrange
        String password = "qwQW12!@";

        RegisterRequest request =
                new RegisterRequest(
                        "Luiz",
                        "Gustavo",
                        "luiz@email.com",
                        password
                );

        Role role = new Role();
        role.setName(
                RolesEnum.BASIC.getAuthority()
        );

        User user = new User();
        user.setId(UUID.randomUUID());

        when(_userRepository.findByEmail(
                request.email()
        )).thenReturn(Optional.empty());

        when(_roleRepository.findByName(
                RolesEnum.BASIC.getAuthority()
        )).thenReturn(Optional.of(role));

        when(_authMapping.toUserEntity(
                eq(request),
                anyString(),
                anySet()
        )).thenReturn(user);

        when(_userRepository.save(user))
                .thenReturn(user);

        Jwt jwt = mock(Jwt.class);

        when(_jwtEncoder.encode(any()))
                .thenReturn(jwt);

        when(jwt.getTokenValue())
                .thenReturn("fake.jwt");

        when(_authMapping.toLoginResponse(
                anyString(),
                any()
        )).thenReturn(mock(LoginResponse.class));

        // Act
        _authService.register(request);

        // Assert
        ArgumentCaptor<String> passwordCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(_authMapping)
                .toUserEntity(
                        eq(request),
                        passwordCaptor.capture(),
                        anySet()
                );

        String encodedPassword =
                passwordCaptor.getValue();

        assertNotEquals(
                password,
                encodedPassword
        );

        assertTrue(
                _bCryptPasswordEncoder.matches(
                        password,
                        encodedPassword
                )
        );
    }


    @Test
    void shouldAssignBasicRoleToNewUser() {

        // Arrange
        RegisterRequest request =
                new RegisterRequest(
                        "Luiz",
                        "Gustavo",
                        "luiz@email.com",
                        "qwQW12!@"
                );

        Role basicRole = new Role();
        basicRole.setName(
                RolesEnum.BASIC.getAuthority()
        );

        User user = new User();
        user.setId(UUID.randomUUID());

        when(_userRepository.findByEmail(
                request.email()
        )).thenReturn(Optional.empty());

        when(_roleRepository.findByName(
                RolesEnum.BASIC.getAuthority()
        )).thenReturn(Optional.of(basicRole));

        when(_authMapping.toUserEntity(
                eq(request),
                anyString(),
                anySet()
        )).thenReturn(user);

        when(_userRepository.save(user))
                .thenReturn(user);

        Jwt jwt = mock(Jwt.class);

        when(_jwtEncoder.encode(any()))
                .thenReturn(jwt);

        when(jwt.getTokenValue())
                .thenReturn("fake.jwt");

        when(_authMapping.toLoginResponse(
                anyString(),
                any()
        )).thenReturn(mock(LoginResponse.class));

        // Act
        _authService.register(request);

        // Assert

        ArgumentCaptor<Set<Role>> roleCaptor =
                ArgumentCaptor.forClass(Set.class);

        verify(_authMapping)
                .toUserEntity(
                        eq(request),
                        anyString(),
                        roleCaptor.capture()
                );

        Set<Role> roles =
                roleCaptor.getValue();

        assertEquals(
                1,
                roles.size()
        );

        assertTrue(
                roles.contains(basicRole)
        );
    }


    // Login method
    @Test
    void shouldLoginUserSuccessfully() {

        // ARRANGE
        UUID userId = UUID.randomUUID();

        String email = "luiz@email.com";
        String password = "qwQW12!@";
        String fakeJwt = "fake.jwt.token";

        LoginRequest request = new LoginRequest(
                email,
                password
        );

        // User stored in database
        User loginUser = new User();

        loginUser.setId(userId);
        loginUser.setEmail(email);

        loginUser.setPassword(
                _bCryptPasswordEncoder.encode(password)
        );

        LoginResponse expectedLoginResponse =
                mock(LoginResponse.class);


        // User exists
        when(_userRepository.findByEmail(email))
                .thenReturn(Optional.of(loginUser));


        // JWT generation
        Jwt jwt = mock(Jwt.class);

        when(_jwtEncoder.encode(
                any(JwtEncoderParameters.class)
        )).thenReturn(jwt);

        when(jwt.getTokenValue())
                .thenReturn(fakeJwt);


        // LoginResponse generation
        when(_authMapping.toLoginResponse(
                anyString(),
                any()
        )).thenReturn(expectedLoginResponse);


        // ACT
        ResponseEntity<LoginResponse> response =
                _authService.login(request);


        // ASSERT
        assertNotNull(response);

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                expectedLoginResponse,
                response.getBody()
        );


        // VERIFY
        verify(_userRepository)
                .findByEmail(email);

        verify(_jwtEncoder)
                .encode(
                        any(JwtEncoderParameters.class)
                );

        verify(_authMapping)
                .toLoginResponse(
                        eq(fakeJwt),
                        any()
                );
    }


    @Test
    void shouldThrowBadCredentialsWhenUserDoesNotExist() {

        // ARRANGE
        String email = "notfound@email.com";

        LoginRequest request = new LoginRequest(
                email,
                "qwQW12!@"
        );

        when(_userRepository.findByEmail(email))
                .thenReturn(Optional.empty());


        // ACT + ASSERT
        BadCredentialsException exception =
                assertThrows(
                        BadCredentialsException.class,
                        () -> _authService.login(request)
                );


        // ASSERT EXCEPTION
        assertEquals(
                "Invalid credentials for this user.",
                exception.getMessage()
        );


        // VERIFY
        verify(_userRepository)
                .findByEmail(email);

        // Nothing after authentication failure
        verify(_jwtEncoder, never())
                .encode(any(JwtEncoderParameters.class));

        verify(_authMapping, never())
                .toLoginResponse(
                        anyString(),
                        any()
                );
    }


    @Test
    void shouldThrowBadCredentialsWhenPasswordIsInvalid() {

        // ARRANGE
        UUID userId = UUID.randomUUID();

        String email = "luiz@email.com";

        String correctPassword = "qwQW12!@";
        String invalidPassword = "wrongPassword";

        LoginRequest request = new LoginRequest(
                email,
                invalidPassword
        );

        User loginUser = new User();

        loginUser.setId(userId);
        loginUser.setEmail(email);

        loginUser.setPassword(
                _bCryptPasswordEncoder.encode(correctPassword)
        );


        when(_userRepository.findByEmail(email))
                .thenReturn(Optional.of(loginUser));


        // ACT + ASSERT
        BadRequestException exception =
                assertThrows(
                        BadRequestException.class,
                        () -> _authService.login(request)
                );


        // ASSERT
        assertEquals(
                "Invalid credentials for this user.",
                exception.getMessage()
        );


        // VERIFY
        verify(_userRepository)
                .findByEmail(email);

        // JWT must NOT be generated
        verify(_jwtEncoder, never())
                .encode(any(JwtEncoderParameters.class));

        // LoginResponse must NOT be generated
        verify(_authMapping, never())
                .toLoginResponse(
                        anyString(),
                        any()
                );
    }

}
