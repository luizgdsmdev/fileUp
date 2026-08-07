package com.bytebybyte.fileup.Application.Services.Auth;

import com.bytebybyte.fileup.Application.DTOs.Request.Auth.LoginRequest;
import com.bytebybyte.fileup.Application.DTOs.Request.Auth.RegisterRequest;
import com.bytebybyte.fileup.Application.DTOs.Response.Auth.LoginResponse;
import com.bytebybyte.fileup.Application.Interfaces.Auth.IAuthService;
import com.bytebybyte.fileup.Application.Utils.Auth.JwtClaimsData;
import com.bytebybyte.fileup.Domain.Entities.User.User;
import com.bytebybyte.fileup.Domain.Exceptions.ConflictException;
import com.bytebybyte.fileup.Domain.Exceptions.TokenGenerationException;
import com.bytebybyte.fileup.Infrastructure.Persistence.Interfaces.User.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtEncodingException;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

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

    public AuthService(JwtEncoder jwtEncoder,
                       BCryptPasswordEncoder bCryptPasswordEncoder,
                       UserRepository userRepository) {

        this._jwtEncoder = jwtEncoder;
        this._bCryptPasswordEncoder = bCryptPasswordEncoder;
        this._userRepository = userRepository;
    }


    /**
     * Method to handle the login request and generate the JWT token.
     * Requires the supportive methods to validate the password and generate the JWT claims set.
     * @param loginRequest LoginRequest object containing the user credentials from the controller layer (email and password)
     * @return ResponseEntity<LoginResponse> containing the JWT token and the expiration date
     */
    public ResponseEntity<LoginResponse> login(LoginRequest loginRequest){

        // Checks for user existence
        User loginUser = _userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials for this user."));

        // Supportive method to validate password, throws exception if invalid
        _validatePassword(loginRequest.password(), loginUser.getPassword(), "login");


        // Starts the JWT generation process once the user is validated
        JwtClaimsData claimsData = _generateJwtClaimsSet(loginUser.getId().toString(), "login");

        // Generates the JWT token based on the claims received
        var jwtKey = _jwtEncoder.encode(JwtEncoderParameters.from(claimsData.claims())).getTokenValue();

        return ResponseEntity.ok(
                new LoginResponse(jwtKey, claimsData.expiration())
        );
    }



    public ResponseEntity<LoginResponse> register(RegisterRequest registerRequest){
        // Validates if user already exists
        if(_userRepository.findByEmail(registerRequest.email()).isPresent()){
            throw new ConflictException("This email is already in use", "AuthService_register_method");
        }



        return null;
    }


    // Supportive methods init -------

    /**
     * Validates the password against the hashed password, throws BadCredentialsException if invalid.
     * I decided to use it as a separate method to avoid repeating the same code in the login method and
     * for future support, in case we need to validate the password in other places or in a different way.
     * @param rawPassword Raw password to be validated (from loginRequest)
     * @param hashedPassword Hashed password from the database (from user)
     */
    private void _validatePassword(String rawPassword, String hashedPassword, String methodCall){
        if(!_bCryptPasswordEncoder.matches(rawPassword, hashedPassword)){
            throw new BadCredentialsException("Invalid credentials for this user. \n Issuer: AuthService_"+ methodCall + "_method");
        }
    }

    /**
     * Generates the JWT claims set, including the expiration date.
     * Based on the user id, the subject is set to the user id.
     * @param userId User id to be used as the subject in the JWT claims set
     * @return JwtClaimsData object containing the JwtClaimsSet and the expiration date
     */
    private JwtClaimsData _generateJwtClaimsSet(String userId, String methodCall){

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
                    ". \nIssuer: AuthService_method"+ methodCall + "_method");
        }
    }

    // Supportive methods end -------

}
