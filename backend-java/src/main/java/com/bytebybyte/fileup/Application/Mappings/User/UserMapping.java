package com.bytebybyte.fileup.Application.Mappings.User;

import com.bytebybyte.fileup.Application.DTOs.Response.User.UserResponse;
import com.bytebybyte.fileup.Domain.Entities.User.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapping {

    /**
     * Mapping userEntity to userEntity response, most used in controller -> GET
     * @param userEntity UserEntity entity
     * @return UserResponse DTO
     */
    public UserResponse toResponse(UserEntity userEntity) {

        return new UserResponse(
                userEntity.getFirstName(),
                userEntity.getSecondName(),
                userEntity.getEmail(),
                userEntity.getId()
        );

    }

    
}
