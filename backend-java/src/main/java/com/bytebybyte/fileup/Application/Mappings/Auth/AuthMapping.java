package com.bytebybyte.fileup.Application.Mappings.Auth;


import com.bytebybyte.fileup.Application.DTOs.Request.Auth.RegisterRequest;
import com.bytebybyte.fileup.Application.DTOs.Response.Auth.LoginResponse;
import com.bytebybyte.fileup.Domain.Entities.Roles.Role;
import com.bytebybyte.fileup.Domain.Entities.User.User;

import java.time.Instant;
import java.util.Set;

public class AuthMapping {

    /**
     * Method to map the RegisterRequest DTO to the User entity.
     * @param registerRequest DTO from request
     * @param encodedPassword Encrypted password type String
     * @param roleSet Role set type Set<Role>
     * @return User entity
     */
    public User toUserEntity(RegisterRequest registerRequest,
                             String encodedPassword,
                             Set<Role> roleSet) {

        User user = new User();
        user.setFirstName(registerRequest.firstName());
        user.setSecondName(registerRequest.secondName());
        user.setEmail(registerRequest.email());
        user.setPassword(encodedPassword);
        user.setRoleSet(roleSet);

        return user;
    }

    /**
     * Method to map the JWT token and expiration time to the LoginResponse DTO.
     * @param accessToken JWT token type String
     * @param expiresIn Expiration time type Instant
     * @return LoginResponse DTO
     */
    public LoginResponse toLoginResponse(String accessToken,
                                         Instant expiresIn){

        return new LoginResponse(accessToken, expiresIn);
    }
}
