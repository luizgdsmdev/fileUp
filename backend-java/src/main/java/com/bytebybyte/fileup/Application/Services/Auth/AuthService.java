package com.bytebybyte.fileup.Application.Services.Auth;

import com.bytebybyte.fileup.Application.DTOs.Request.Auth.LoginRequest;
import com.bytebybyte.fileup.Application.DTOs.Response.Auth.LoginResponse;
import com.bytebybyte.fileup.Application.Utils.Auth.JwtClaimsData;
import com.bytebybyte.fileup.Domain.Entities.User.User;
import com.bytebybyte.fileup.Infrastructure.Persistence.Interfaces.User.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService {
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



    public ResponseEntity<LoginResponse> login(LoginRequest loginRequest){

        // Checks for user existence
        User loginUser = _userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials for this user."));

        // Supportive method to validate password, throws exception if invalid
        _validatePassword(loginRequest.password(), loginUser.getPassword());


        // Starts the JWT generation process once the user is validated
        JwtClaimsData claimsData = _generateJwtClaimsSet(loginUser.getId().toString());

        // Generates the JWT token based on the claims received
        var jwtKey = _jwtEncoder.encode(JwtEncoderParameters.from(claimsData.claims())).getTokenValue();

        return ResponseEntity.ok(
                new LoginResponse(jwtKey, claimsData.expiration())
        );
    }


    // Supportive methods init -------

    /**
     * Validates the password against the hashed password, throws BadCredentialsException if invalid.
     * I decided to use it as a separate method to avoid repeating the same code in the login method and
     * for future support, in case we need to validate the password in other places or in a different way.
     * @param password Raw password to be validated (from loginRequest)
     * @param hashedPassword Hashed password from the database (from user)
     */
    private void _validatePassword(String password, String hashedPassword){
        if(!_bCryptPasswordEncoder.matches(password, hashedPassword)){
            throw new BadCredentialsException("Invalid credentials for this user.");
        }
    }

    /**
     * Generates the JWT claims set, including the expiration date.
     * Based on the user id, the subject is set to the user id.
     * @param userId User id to be used as the subject in the JWT claims set
     * @return JwtClaimsData object containing the JwtClaimsSet and the expiration date
     */
    private JwtClaimsData _generateJwtClaimsSet(String userId){

        var now = Instant.now();
        var expiration = now.plusSeconds(3600); // 1 hour

        var claims = JwtClaimsSet.builder()
                .issuer("fileup_backend_AuthService_login_method")
                .subject(userId)
                .issuedAt(now)
                .expiresAt(expiration)
                .build();

        return new JwtClaimsData(claims, expiration);
    }

    // Supportive methods end -------



}
