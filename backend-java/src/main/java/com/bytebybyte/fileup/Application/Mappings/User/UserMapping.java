package com.bytebybyte.fileup.Application.Mappings.User;

import com.bytebybyte.fileup.Application.DTOs.Response.User.UserResponse;
import com.bytebybyte.fileup.Domain.Entities.User.User;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class UserMapping {

    /**
     * Mapping user to user response, most used in controller -> GET
     * @param user User entity
     * @return UserResponse DTO
     */
    public UserResponse toResponse(User user) {

        return new UserResponse(
                user.getFirstName(),
                user.getSecondName(),
                user.getEmail(),
                user.getId()
        );

    }

    
}
