package com.bytebybyte.fileup.Application.Mappings.Auth;


import com.bytebybyte.fileup.Application.DTOs.Request.Auth.RegisterRequest;
import com.bytebybyte.fileup.Application.DTOs.Response.Auth.LoginResponse;
import com.bytebybyte.fileup.Domain.Entities.Roles.Role;
import com.bytebybyte.fileup.Domain.Entities.User.UserEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;

@Component
public class AuthMapping {

    /**
     * Method to map the RegisterRequest DTO to the UserEntity entity.
     * @param registerRequest DTO from request
     * @param encodedPassword Encrypted password type String
     * @param roleSet Role set type Set<Role>
     * @return UserEntity entity
     */
    public UserEntity toUserEntity(RegisterRequest registerRequest,
                                   String encodedPassword,
                                   Set<Role> roleSet) {

        UserEntity userEntity = new UserEntity();
        userEntity.setFirstName(registerRequest.firstName());
        userEntity.setSecondName(registerRequest.secondName());
        userEntity.setEmail(registerRequest.email());
        userEntity.setPassword(encodedPassword);
        userEntity.setRoleSet(roleSet);

        return userEntity;
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
