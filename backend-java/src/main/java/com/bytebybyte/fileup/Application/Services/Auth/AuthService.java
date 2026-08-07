package com.bytebybyte.fileup.Application.Services.Auth;

import com.bytebybyte.fileup.Application.DTOs.Request.Auth.LoginRequest;
import com.bytebybyte.fileup.Application.DTOs.Request.Auth.RegisterRequest;
import com.bytebybyte.fileup.Application.DTOs.Response.Auth.LoginResponse;
import com.bytebybyte.fileup.Application.Interfaces.Auth.IAuthService;
import com.bytebybyte.fileup.Application.Mappings.Auth.AuthMapping;
import com.bytebybyte.fileup.Application.Utils.Auth.JwtClaimsData;
import com.bytebybyte.fileup.Domain.Entities.Roles.Role;
import com.bytebybyte.fileup.Domain.Entities.User.User;
import com.bytebybyte.fileup.Domain.Enums.Roles.RolesEnum;
import com.bytebybyte.fileup.Domain.Exceptions.BadRequestException;
import com.bytebybyte.fileup.Domain.Exceptions.ConflictException;
import com.bytebybyte.fileup.Domain.Exceptions.NotFoundException;
import com.bytebybyte.fileup.Domain.Exceptions.TokenGenerationException;
import com.bytebybyte.fileup.Infrastructure.Persistence.Interfaces.Roles.RolesRepository;
import com.bytebybyte.fileup.Infrastructure.Persistence.Interfaces.User.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.time.Instant;
import java.util.Set;

/**
 * Service class for handling authentication-related operations.
 * Implements the IAuthService interface to provide methods for user login and registration.
 * Responsible for generating JWT tokens and validating user credentials.
 * @method login(LoginRequest) -> ResponseEntity(LoginResponse)
 * @method register(LoginRequest) -> ResponseEntity(LoginResponse)
 */
@Slf4j
@Service
public class AuthService implements IAuthService {
    private final JwtEncoder _jwtEncoder;
    private final BCryptPasswordEncoder _bCryptPasswordEncoder;
    private final UserRepository _userRepository;
    private final RolesRepository _roleRepository;
    private final AuthMapping _authMapping = new AuthMapping();

    public AuthService(JwtEncoder jwtEncoder,
                       BCryptPasswordEncoder bCryptPasswordEncoder,
                       UserRepository userRepository,
                       RolesRepository roleRepository) {

        this._jwtEncoder = jwtEncoder;
        this._bCryptPasswordEncoder = bCryptPasswordEncoder;
        this._userRepository = userRepository;
        this._roleRepository = roleRepository;
    }


    /**
     * Method to handle the login request and generate the JWT token.
     * Requires the supportive methods to validate the password and handle the user authentication.
     * @param loginRequest LoginRequest object containing the user credentials from the controller layer (email and password)
     * @return ResponseEntity<LoginResponse> containing the JWT token and the expiration date
     */
    public ResponseEntity<LoginResponse> login(LoginRequest loginRequest){

        // Checks for user existence
        User loginUser = _userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials for this user."));

        // Supportive method to validate password, throws exception if invalid
        _validatePassword(loginRequest.password(), loginUser.getPassword());


        // Supportive method to init claims and JWT generation process, returning
        // the JWT token and the expiration date on LoginResponse type
        LoginResponse response = _authHandler(loginUser.getId().toString());
        return ResponseEntity.ok(response);
    }

    /**
     * Method to handle the registration request and create a new user.
     * Requires the supportive methods to encrypt the password and handle the user authentication.
     * @param registerRequest RegisterRequest object containing the user data from the controller layer
     * @return ResponseEntity<LoginResponse> containing the JWT token and the expiration date
     */
    public ResponseEntity<LoginResponse> register(RegisterRequest registerRequest){
        // Validates if user already exists
        if(_userRepository.findByEmail(registerRequest.email()).isPresent()){
            throw new ConflictException("Unable to use this data to create a user", "AuthService_register_method");
        }

        // Recover the role from the database to a Set of roles
        Role role  = _roleRepository.findByName(
                      RolesEnum.BASIC.getAuthority())
                      .orElseThrow(() ->
                      new NotFoundException("Role " + RolesEnum.BASIC.name() + " not fund.",
                                            "AuthService_register_method" ));
        Set<Role> roleSet = Set.of(role);


        // Encrypt the password
        String encodedPassword = _bCryptPasswordEncoder.encode(registerRequest.password());

        // User entity creation
        User newUser = _authMapping.toUserEntity(registerRequest, encodedPassword, roleSet);

        // Save the user to the database, get us the ID generated
        User savedUser = _userRepository.save(newUser);

        // Supportive method to init claims and JWT generation process, returning
        // the JWT token and the expiration date on LoginResponse type
        //TODO: Create a UserController to handle the user creation and update operations with /api/v1/users/
        URI location = URI.create("/api/v1/users/" + savedUser.getId());
        LoginResponse response = _authHandler(savedUser.getId().toString());

        return ResponseEntity
                .created(location)
                .body(response);
    }


    // Supportive methods init -------

    /**
     * handles the claims and JWT generation process.
     * @param userID User id to be used as the subject in the JWT claims set
     * @return LoginResponse containing the JWT token and the expiration date
     */
    public LoginResponse _authHandler(String userID){
        // Init login process, to return the JWT token and redirect to the home page
        // Starts the JWT generation process once the user is validated
        JwtClaimsData claimsData = _generateJwtClaimsSet(userID);

        // Generates the JWT token based on the claims received
        var jwtKey = _jwtEncoder.encode(JwtEncoderParameters.from(claimsData.claims())).getTokenValue();

        return _authMapping.toLoginResponse(jwtKey, claimsData.expiration());
    }


    /**
     * Validates the password against the hashed password, throws BadCredentialsException if invalid.
     * I decided to use it as a separate method to avoid repeating the same code in the login method and
     * for future support, in case we need to validate the password in other places or in a different way.
     *
     * @param rawPassword    Raw password to be validated (from loginRequest)
     * @param hashedPassword Hashed password from the database (from user)
     */
    private void _validatePassword(String rawPassword, String hashedPassword){
        if(!_bCryptPasswordEncoder.matches(rawPassword, hashedPassword)){
            throw new BadRequestException("Invalid credentials for this user.", "AuthService_login_method");
        }
    }

    /**
     * Generates the JWT claims set, including the expiration date.
     * Based on the user id, the subject is set to the user id.
     *
     * @param userId User id to be used as the subject in the JWT claims set
     * @return JwtClaimsData object containing the JwtClaimsSet and the expiration date
     */
    private JwtClaimsData _generateJwtClaimsSet(String userId){

        try{
            var now = Instant.now();
            var expiration = now.plusSeconds(3600); // 1 hour

            var claims = JwtClaimsSet.builder()
                    .issuer("fileup_backend_AuthService_login_method")
                    .subject(userId)
                    .issuedAt(now)
                    .expiresAt(expiration)
                    .build();

            return new JwtClaimsData(claims, expiration);

        } catch (Exception e) {
            throw new TokenGenerationException(
                    "Error to generate JWT for new user: " + e,
                    "AuthService_method_login_method");
        }
    }

    // Supportive methods end -------

}
