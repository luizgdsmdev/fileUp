package com.bytebybyte.fileup.Application.Services.User;

import com.bytebybyte.fileup.Application.DTOs.Request.User.UserUpdateRequest;
import com.bytebybyte.fileup.Application.DTOs.Response.User.UserResponse;
import com.bytebybyte.fileup.Application.Mappings.User.UserMapping;
import com.bytebybyte.fileup.Domain.Entities.User.UserEntity;
import com.bytebybyte.fileup.Domain.Exceptions.ConflictException;
import com.bytebybyte.fileup.Domain.Exceptions.NotFoundException;
import com.bytebybyte.fileup.Infrastructure.Persistence.Interfaces.Users.IUserRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {
    private final IUserRepository _IuserRepository;
    private final UserMapping _userMapping;
    private final BCryptPasswordEncoder _bCryptPasswordEncoder;

    /**
     * Method to get a user by ID.
     * @param userId UserEntity ID -> UUID type
     * @return ResponseEntity<UserResponse>
     */
    public ResponseEntity<UserResponse> get(UUID userId){
        //Get UserEntity from DB
        UserEntity userEntity = _IuserRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("No userEntity found with this ID.",
                                                             "UserService_get_method"));

        // Mapping to UserResponse DTO
        UserResponse response = _userMapping.toResponse(userEntity);

        return ResponseEntity.ok(response);
    }


    /**
     * Method to update a user by ID.
     * @param userId UserEntity ID -> UUID type
     * @param userUpdateRequest UserUpdateRequest DTO from the controller layer
     * @return ResponseEntity<UserResponse> with the updated user
     */
    public ResponseEntity<UserResponse> update(@Valid UUID userId, UserUpdateRequest userUpdateRequest) {
        //Get UserEntity from DB
        UserEntity userEntity = _IuserRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("No userEntity found with this ID.",
                                                             "UserService_update_method"));

        // Update userEntity fields
        _updateUserFields(userEntity, userUpdateRequest);

        // Save the userEntity to the database
        UserEntity savedUserEntity = _IuserRepository.save(userEntity);

        // Mapping to UserResponse DTO
        UserResponse response = _userMapping.toResponse(savedUserEntity);

        return ResponseEntity.ok(response);

    }


    // Supportive methods init -------

    /**
     * Method to update the userEntity fields based on the request.
     * Updates the email, first name, second name, and password.
     * @param userEntity UserEntity entity
     * @param request UserUpdateRequest DTO from the controller layer
     */
    private void _updateUserFields(UserEntity userEntity, UserUpdateRequest request) {

        if (request.email() != null && !request.email().isBlank()) {

            // If the email was sent to update, first we check if the new email is already in use
            if(!request.email().equals(userEntity.getEmail())){

                _IuserRepository.findByEmail(request.email())
                        .ifPresent(u -> {
                            throw new ConflictException("This email cannot be used.", "UserService_update_method");
                        });
            }

            // If new email is not in use, we update the userEntity
            userEntity.setEmail(request.email());
        }

        if (request.firstName() != null && !request.firstName().isBlank()) {
            userEntity.setFirstName(request.firstName());
        }

        if (request.secondName() != null && !request.secondName().isBlank()) {
            userEntity.setSecondName(request.secondName());
        }

        if (request.password() != null && !request.password().isBlank()) {
            // Encrypt the password
            String encodedPassword = _bCryptPasswordEncoder.encode(request.password());

            userEntity.setPassword(encodedPassword);
        }
    }
}
