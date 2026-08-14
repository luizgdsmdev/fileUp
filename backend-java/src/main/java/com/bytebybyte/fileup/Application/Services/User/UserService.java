package com.bytebybyte.fileup.Application.Services.User;

import com.bytebybyte.fileup.Application.DTOs.Request.User.UserUpdateRequest;
import com.bytebybyte.fileup.Application.DTOs.Response.User.UserResponse;
import com.bytebybyte.fileup.Application.Mappings.User.UserMapping;
import com.bytebybyte.fileup.Domain.Entities.User.User;
import com.bytebybyte.fileup.Domain.Exceptions.ConflictException;
import com.bytebybyte.fileup.Domain.Exceptions.NotFoundException;
import com.bytebybyte.fileup.Infrastructure.Persistence.Interfaces.User.UserRepository;
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
    private final UserRepository _userRepository;
    private final UserMapping _userMapping;
    private final BCryptPasswordEncoder _bCryptPasswordEncoder;

    /**
     * Method to get a user by ID.
     * @param userId User ID -> UUID type
     * @return ResponseEntity<UserResponse>
     */
    public ResponseEntity<UserResponse> get(UUID userId){
        //Get User from DB
        User user = _userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("No user found with this ID.",
                                                             "UserService_get_method"));

        // Mapping to UserResponse DTO
        UserResponse response = _userMapping.toResponse(user);

        return ResponseEntity.ok(response);
    }


    /**
     * Method to update a user by ID.
     * @param userId User ID -> UUID type
     * @param userUpdateRequest UserUpdateRequest DTO from the controller layer
     * @return ResponseEntity<UserResponse> with the updated user
     */
    public ResponseEntity<UserResponse> update(@Valid UUID userId, UserUpdateRequest userUpdateRequest) {
        //Get User from DB
        User user = _userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("No user found with this ID.",
                                                             "UserService_update_method"));

        // Update user fields
        _updateUserFields(user, userUpdateRequest);

        // Save the user to the database
        User savedUser = _userRepository.save(user);

        // Mapping to UserResponse DTO
        UserResponse response = _userMapping.toResponse(savedUser);

        return ResponseEntity.ok(response);

    }


    // Supportive methods init -------

    /**
     * Method to update the user fields based on the request.
     * Updates the email, first name, second name, and password.
     * @param user User entity
     * @param request UserUpdateRequest DTO from the controller layer
     */
    private void _updateUserFields(User user, UserUpdateRequest request) {

        if (request.email() != null && !request.email().isBlank()) {

            // If the email was sent to update, first we check if the new email is already in use
            if(!request.email().equals(user.getEmail())){

                _userRepository.findByEmail(request.email())
                        .ifPresent(u -> {
                            throw new ConflictException("This email cannot be used.", "UserService_update_method");
                        });
            }

            // If new email is not in use, we update the user
            user.setEmail(request.email());
        }

        if (request.firstName() != null && !request.firstName().isBlank()) {
            user.setFirstName(request.firstName());
        }

        if (request.secondName() != null && !request.secondName().isBlank()) {
            user.setSecondName(request.secondName());
        }

        if (request.password() != null && !request.password().isBlank()) {
            // Encrypt the password
            String encodedPassword = _bCryptPasswordEncoder.encode(request.password());

            user.setPassword(encodedPassword);
        }
    }
}
